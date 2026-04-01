package com.econocom.auth.exception;

/**
 * Base exception for authentication-related errors.
 */
public class AuthenticationException extends RuntimeException {
    
    public AuthenticationException(String message) {
        super(message);
    }
}
