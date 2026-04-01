package com.econocom.auth.exception;

import org.junit.Test;

import static org.junit.Assert.*;

public class InvalidCredentialsExceptionTest {

    private static final String DEFAULT_MESSAGE = "Invalid credentials";

    @Test
    public void constructor_DefaultMessage_SetsCorrectMessage() {
        
        InvalidCredentialsException exception = new InvalidCredentialsException();

        assertEquals(DEFAULT_MESSAGE, exception.getMessage());
    }

    @Test
    public void exception_IsRuntimeException() {
        
        InvalidCredentialsException exception = new InvalidCredentialsException();

        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    public void exception_IsAuthenticationException() {
        
        InvalidCredentialsException exception = new InvalidCredentialsException();

        assertTrue(exception instanceof AuthenticationException);
    }

    @Test
    public void constructor_HasNullCause() {
        
        InvalidCredentialsException exception = new InvalidCredentialsException();

        assertNull(exception.getCause());
    }
}