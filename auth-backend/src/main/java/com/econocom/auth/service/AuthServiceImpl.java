package com.econocom.auth.service;

import com.econocom.auth.exception.InvalidCredentialsException;
import com.econocom.auth.exception.SsoException;
import com.econocom.auth.model.LoginResponse;
import com.econocom.auth.model.Usuario;
import com.econocom.auth.repository.UsuarioRepository;
import com.econocom.auth.util.JwtUtil;
import com.econocom.auth.util.LoggingUtil;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private static final String CLASS_NAME = AuthServiceImpl.class.getSimpleName();


    private static final String TOKEN_TYPE = "Bearer";
    private static final String SSO_EMAIL = "sso@econocom.com";

    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long jwtRefreshExpiration;

    @Autowired
    public AuthServiceImpl(JwtUtil jwtUtil, UsuarioRepository usuarioRepository) {

        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public LoginResponse authenticate(String email, String password) {

        String methodName = (new Throwable()).getStackTrace()[0].getMethodName();


        LoggingUtil.log(log, LoggingUtil.INFO, CLASS_NAME, methodName, "Authenticating user");

        if (StringUtils.isAllBlank(email) || StringUtils.isAllBlank(password)) {

            throw new InvalidCredentialsException();
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                                           .orElseThrow(InvalidCredentialsException::new);

        LoggingUtil.log(log, LoggingUtil.DEBUG, CLASS_NAME, methodName, 
                        String.format("User data: %s", usuario.toString()));

        if (!password.equals(usuario.getPassword())) {

            throw new InvalidCredentialsException();
        }

        if (!usuario.isActivo()) {

            throw new InvalidCredentialsException();
        }

        String token = jwtUtil.generateToken(email);
        String refreshToken = jwtUtil.generateRefreshToken(email);

        String userName = usuario.getNombre();

        LoggingUtil.log(log, LoggingUtil.DEBUG, CLASS_NAME, methodName, 
                        String.format("Token generated for user (%s): %s", userName, token));
        
        LoggingUtil.log(log, LoggingUtil.DEBUG, CLASS_NAME, methodName, 
                        String.format("RefreshToken generated for user (%s): %s", userName, refreshToken));


        LoggingUtil.log(log, LoggingUtil.INFO, CLASS_NAME, methodName, "User authenticated");

        return new LoginResponse(token, refreshToken, jwtExpiration, TOKEN_TYPE);
    }

    @Override
    public LoginResponse authenticateSso(String code) {

        String methodName = (new Throwable()).getStackTrace()[0].getMethodName();


        LoggingUtil.log(log, LoggingUtil.INFO, CLASS_NAME, methodName, "Checking 'code'");

        if (StringUtils.isAllBlank(code)) {
            
            throw new SsoException("SSO authorization code is required");
        }

        usuarioRepository.findByEmail(SSO_EMAIL)
                         .orElseThrow(() -> new SsoException("SSO user not configured"));

        String token = jwtUtil.generateToken(SSO_EMAIL);
        String refreshToken = jwtUtil.generateRefreshToken(SSO_EMAIL);

        LoggingUtil.log(log, LoggingUtil.DEBUG, CLASS_NAME, methodName, 
                        String.format("Token generated: %s", token));
        LoggingUtil.log(log, LoggingUtil.DEBUG, CLASS_NAME, methodName, 
                        String.format("Refresh token generated: %s", refreshToken));

        return new LoginResponse(token, refreshToken, jwtExpiration, TOKEN_TYPE);
    }
}
