package com.econocom.auth.exception;

public class InvalidCredentialsException extends AuthenticationException {
    
    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}
