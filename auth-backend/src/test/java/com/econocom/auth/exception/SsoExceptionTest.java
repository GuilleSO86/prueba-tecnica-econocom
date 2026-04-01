package com.econocom.auth.exception;

import org.junit.Test;

import static org.junit.Assert.*;

public class SsoExceptionTest {

    private static final String CUSTOM_MESSAGE = "SSO code expired";

    @Test
    public void constructor_CustomMessage_SetsCustomMessage() {
        
        SsoException exception = new SsoException(CUSTOM_MESSAGE);

        assertEquals(CUSTOM_MESSAGE, exception.getMessage());
    }

    @Test
    public void exception_IsRuntimeException() {
        
        SsoException exception = new SsoException(CUSTOM_MESSAGE);

        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    public void exception_IsAuthenticationException() {
        
        SsoException exception = new SsoException(CUSTOM_MESSAGE);

        assertTrue(exception instanceof AuthenticationException);
    }

    @Test
    public void constructor_HasNullCause() {
        
        SsoException exception = new SsoException(CUSTOM_MESSAGE);

        assertNull(exception.getCause());
    }

    @Test
    public void constructor_DifferentMessage_SetsCorrectly() {
        
        String anotherMessage = "SSO user not configured";

        SsoException exception = new SsoException(anotherMessage);

        assertEquals(anotherMessage, exception.getMessage());
    }
}