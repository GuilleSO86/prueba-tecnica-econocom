package com.econocom.auth.controller;

import com.econocom.auth.model.LoginRequest;
import com.econocom.auth.model.LoginResponse;
import com.econocom.auth.service.AuthService;
import com.econocom.auth.util.LoggingUtil;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String CLASS_NAME = AuthController.class.getSimpleName();


    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {

        String methodName = (new Throwable()).getStackTrace()[0].getMethodName();
        

        LoggingUtil.log(log, LoggingUtil.INFO, CLASS_NAME, methodName, "Request to 'login' path");

        LoginResponse response = authService.authenticate(
                                                            loginRequest.getEmail(),
                                                            loginRequest.getPassword()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * SSO endpoint - initiates SSO login flow.
     */
    @GetMapping("/sso")
    public ResponseEntity<Map<String, String>> initiateSso() {

        String methodName = (new Throwable()).getStackTrace()[0].getMethodName();
        

        LoggingUtil.log(log, LoggingUtil.INFO, CLASS_NAME, methodName, "Request to 'sso' path");

        // Generate state for CSRF protection
        String state = UUID.randomUUID().toString();
        
        // Simulated SSO provider URL (in production, this would be a real SSO provider)
        String ssoProviderUrl = "http://localhost:8080/api/auth/sso/callback?code=simulated-sso-code&state=" + state;

        Map<String, String> response = new HashMap<>();

        response.put("ssoUrl", ssoProviderUrl);
        
        return ResponseEntity.ok(response);
    }

    /**
     * SSO callback endpoint - handles the response from SSO provider.
     * 
     * @param code Authorization code from SSO provider
     * @param state State parameter for CSRF validation
     * 
     * @return JWT token on success
     */
    @GetMapping("/sso/callback")
    public ResponseEntity<LoginResponse> handleSsoCallback(
                                                            @RequestParam(required = false) String code,
                                                            @RequestParam(required = false) String state) {

        String methodName = (new Throwable()).getStackTrace()[0].getMethodName();


        LoggingUtil.log(log, LoggingUtil.INFO, CLASS_NAME, methodName, "Request to 'sso/callback' path");
        
        // Validate the authorization code
        if (StringUtils.isAllBlank(code)) {

            throw new IllegalArgumentException("Authorization code is required");
        }
        
        // Simulate SSO validation (in production, this would validate with the real SSO provider)
        if (!code.startsWith("simulated-sso-code")) {
            
            throw new IllegalArgumentException("Invalid SSO authorization code");
        }
        
        LoginResponse response = authService.authenticateSso(code);
        
        return ResponseEntity.ok(response);
    }
}
