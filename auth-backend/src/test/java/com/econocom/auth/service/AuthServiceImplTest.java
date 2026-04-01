package com.econocom.auth.service;

import com.econocom.auth.exception.InvalidCredentialsException;
import com.econocom.auth.exception.SsoException;
import com.econocom.auth.model.LoginResponse;
import com.econocom.auth.model.Usuario;
import com.econocom.auth.repository.UsuarioRepository;
import com.econocom.auth.util.JwtUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuthServiceImplTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private static final String TEST_EMAIL = "test@econocom.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_TOKEN = "test-jwt-token";
    private static final String TEST_REFRESH_TOKEN = "test-refresh-token";
    private static final long JWT_EXPIRATION = 3600000L;
    private static final long REFRESH_EXPIRATION = 86400000L;

    @Before
    public void setUp() {
        
        // Configuring @Value properties using ReflectionTestUtils
        ReflectionTestUtils.setField(authService, "jwtExpiration", JWT_EXPIRATION);
        ReflectionTestUtils.setField(authService, "jwtRefreshExpiration", REFRESH_EXPIRATION);
    }

    @Test
    public void authenticate_ValidCredentials_ReturnsLoginResponse() {
        
        Usuario usuario = createTestUsuario(TEST_EMAIL, TEST_PASSWORD, "Test User", true);
        
        when(usuarioRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(usuario));
        when(jwtUtil.generateToken(TEST_EMAIL)).thenReturn(TEST_TOKEN);
        when(jwtUtil.generateRefreshToken(TEST_EMAIL)).thenReturn(TEST_REFRESH_TOKEN);

        LoginResponse response = authService.authenticate(TEST_EMAIL, TEST_PASSWORD);

        assertNotNull(response);
        assertEquals(TEST_TOKEN, response.getToken());
        assertEquals(TEST_REFRESH_TOKEN, response.getRefreshToken());
        assertEquals(JWT_EXPIRATION, response.getExpiresIn());
        assertEquals("Bearer", response.getType());
        
        verify(usuarioRepository).findByEmail(TEST_EMAIL);
        verify(jwtUtil).generateToken(TEST_EMAIL);
        verify(jwtUtil).generateRefreshToken(TEST_EMAIL);
    }

    @Test(expected = InvalidCredentialsException.class)
    public void authenticate_InvalidEmail_ThrowsInvalidCredentialsException() {
        
        when(usuarioRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        authService.authenticate(TEST_EMAIL, TEST_PASSWORD);
    }

    @Test(expected = InvalidCredentialsException.class)
    public void authenticate_InvalidPassword_ThrowsInvalidCredentialsException() {
        
        Usuario usuario = createTestUsuario(TEST_EMAIL, "differentPassword", "Test User", true);
        
        when(usuarioRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(usuario));

        authService.authenticate(TEST_EMAIL, TEST_PASSWORD);
    }

    @Test(expected = InvalidCredentialsException.class)
    public void authenticate_InactiveUser_ThrowsInvalidCredentialsException() {
        
        Usuario usuario = createTestUsuario(TEST_EMAIL, TEST_PASSWORD, "Test User", false);
        
        when(usuarioRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(usuario));

        authService.authenticate(TEST_EMAIL, TEST_PASSWORD);
    }

    @Test(expected = InvalidCredentialsException.class)
    public void authenticate_NullEmail_ThrowsInvalidCredentialsException() {
        
        authService.authenticate(null, TEST_PASSWORD);
    }

    @Test(expected = InvalidCredentialsException.class)
    public void authenticate_NullPassword_ThrowsInvalidCredentialsException() {
        
        authService.authenticate(TEST_EMAIL, null);
    }

    @Test(expected = InvalidCredentialsException.class)
    public void authenticate_BlankEmail_ThrowsInvalidCredentialsException() {
        
        authService.authenticate("   ", TEST_PASSWORD);
    }

    @Test(expected = InvalidCredentialsException.class)
    public void authenticate_BlankPassword_ThrowsInvalidCredentialsException() {
        
        authService.authenticate(TEST_EMAIL, "   ");
    }

    @Test(expected = InvalidCredentialsException.class)
    public void authenticate_EmptyEmail_ThrowsInvalidCredentialsException() {
        
        authService.authenticate("", TEST_PASSWORD);
    }

    @Test
    public void authenticateSso_ValidCode_ReturnsLoginResponse() {
        
        String ssoCode = "valid-sso-code";
        String ssoEmail = "sso@econocom.com";
        Usuario ssoUser = createTestUsuario(ssoEmail, "sso-password", "SSO User", true);
        
        when(usuarioRepository.findByEmail(ssoEmail)).thenReturn(Optional.of(ssoUser));
        when(jwtUtil.generateToken(ssoEmail)).thenReturn(TEST_TOKEN);
        when(jwtUtil.generateRefreshToken(ssoEmail)).thenReturn(TEST_REFRESH_TOKEN);

        LoginResponse response = authService.authenticateSso(ssoCode);

        assertNotNull(response);
        assertEquals(TEST_TOKEN, response.getToken());
        assertEquals(TEST_REFRESH_TOKEN, response.getRefreshToken());
        assertEquals(JWT_EXPIRATION, response.getExpiresIn());
        
        verify(usuarioRepository).findByEmail(ssoEmail);
        verify(jwtUtil).generateToken(ssoEmail);
        verify(jwtUtil).generateRefreshToken(ssoEmail);
    }

    @Test(expected = SsoException.class)
    public void authenticateSso_NullCode_ThrowsSsoException() {
        
        authService.authenticateSso(null);
    }

    @Test(expected = SsoException.class)
    public void authenticateSso_BlankCode_ThrowsSsoException() {
        
        authService.authenticateSso("   ");
    }

    @Test(expected = SsoException.class)
    public void authenticateSso_EmptyCode_ThrowsSsoException() {
        
        authService.authenticateSso("");
    }

    @Test(expected = SsoException.class)
    public void authenticateSso_SsoUserNotConfigured_ThrowsSsoException() {
        
        String ssoCode = "valid-sso-code";
        String ssoEmail = "sso@econocom.com";
        
        when(usuarioRepository.findByEmail(ssoEmail)).thenReturn(Optional.empty());

        authService.authenticateSso(ssoCode);
    }

    
    private Usuario createTestUsuario(String email, String password, String nombre, boolean activo) {
        
        Usuario usuario = new Usuario();
        
        usuario.setId(1L);
        usuario.setEmail(email);
        usuario.setPassword(password);
        usuario.setNombre(nombre);
        usuario.setActivo(activo);
        
        return usuario;
    }
}