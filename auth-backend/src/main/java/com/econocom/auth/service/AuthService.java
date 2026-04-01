package com.econocom.auth.service;

import com.econocom.auth.model.LoginResponse;

public interface AuthService {
    
    /**
     * Validates credentials against the database.
     *
     * @param email
     * @param password
     * 
     * @return {@link LoginResponse} object containing JWT access token, refresh token, expiration time, and token type.
     */
    LoginResponse authenticate(String email, String password);
    
    /**
     * Validates credentials against the SSO provider.
     *
     * @param code
     * 
     * @return {@link LoginResponse} object containing JWT access token, refresh token, expiration time, and token type.
     */
    LoginResponse authenticateSso(String code);
}