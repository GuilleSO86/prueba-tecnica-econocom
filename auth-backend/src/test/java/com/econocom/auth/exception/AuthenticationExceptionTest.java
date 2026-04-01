package com.econocom.auth.exception;

import org.junit.Test;

import static org.junit.Assert.*;

public class AuthenticationExceptionTest {

    private static final String CUSTOM_MESSAGE = "Authentication failed";

    @Test
    public void constructor_CustomMessage_SetsCustomMessage() {
        
        AuthenticationException exception = new AuthenticationException(CUSTOM_MESSAGE);

        assertEquals(CUSTOM_MESSAGE, exception.getMessage());
    }

    @Test
    public void exception_IsRuntimeException() {
        
        AuthenticationException exception = new AuthenticationException(CUSTOM_MESSAGE);

        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    public void constructor_HasNullCause() {
        
        AuthenticationException exception = new AuthenticationException(CUSTOM_MESSAGE);

        assertNull(exception.getCause());
    }

    @Test
    public void constructor_DifferentMessage_SetsCorrectly() {
        String anotherMessage = "Token validation failed";

        AuthenticationException exception = new AuthenticationException(anotherMessage);

        assertEquals(anotherMessage, exception.getMessage());
    }

    @Test
    public void exception_CanBeExtended() {
        
        AuthenticationException exception = new AuthenticationException(CUSTOM_MESSAGE) {
            // Anonymous class to check that it is not final
        };

        assertNotNull(exception);
        assertEquals(CUSTOM_MESSAGE, exception.getMessage());
    }
}