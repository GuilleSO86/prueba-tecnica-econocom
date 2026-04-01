package com.econocom.auth.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LoggingUtilTest {

    @Mock
    private Logger logger;

    private static final String CLASS_NAME = "TestClass";
    private static final String METHOD_NAME = "testMethod";
    private static final String MESSAGE = "Test message";

    
    @Test
    public void log_InfoLevel_LogsCorrectFormat() {
        
        String expectedFormat = String.format(LoggingUtil.MSG_LOG, 
                                              LoggingUtil.MSG_LOG_PROJECT, 
                                              CLASS_NAME, 
                                              METHOD_NAME, 
                                              MESSAGE);

        LoggingUtil.log(logger, LoggingUtil.INFO, CLASS_NAME, METHOD_NAME, MESSAGE);

        verify(logger).info(expectedFormat);
    }

    @Test
    public void log_DebugLevel_LogsCorrectFormat() {
        
        String expectedFormat = String.format(LoggingUtil.MSG_LOG, 
                                              LoggingUtil.MSG_LOG_PROJECT, 
                                              CLASS_NAME, 
                                              METHOD_NAME, 
                                              MESSAGE);

        LoggingUtil.log(logger, LoggingUtil.DEBUG, CLASS_NAME, METHOD_NAME, MESSAGE);

        verify(logger).debug(expectedFormat);
    }

    @Test
    public void log_ErrorLevel_LogsCorrectFormat() {
        
        String expectedFormat = String.format(LoggingUtil.MSG_LOG, 
                                              LoggingUtil.MSG_LOG_PROJECT, 
                                              CLASS_NAME, 
                                              METHOD_NAME, 
                                              MESSAGE);

        LoggingUtil.log(logger, LoggingUtil.ERROR, CLASS_NAME, METHOD_NAME, MESSAGE);

        verify(logger).error(expectedFormat);
    }

    @Test
    public void log_WarnLevel_LogsCorrectFormat() {
        
        String expectedFormat = String.format(LoggingUtil.MSG_LOG, 
                                              LoggingUtil.MSG_LOG_PROJECT, 
                                              CLASS_NAME, 
                                              METHOD_NAME, 
                                              MESSAGE);

        LoggingUtil.log(logger, LoggingUtil.WARN, CLASS_NAME, METHOD_NAME, MESSAGE);

        verify(logger).warn(expectedFormat);
    }

    @Test
    public void log_TraceLevel_LogsCorrectFormat() {
        
        String expectedFormat = String.format(LoggingUtil.MSG_LOG, 
                                              LoggingUtil.MSG_LOG_PROJECT, 
                                              CLASS_NAME, 
                                              METHOD_NAME, 
                                              MESSAGE);

        LoggingUtil.log(logger, LoggingUtil.TRACE, CLASS_NAME, METHOD_NAME, MESSAGE);

        verify(logger).trace(expectedFormat);
    }

    @Test
    public void log_UnknownLevel_DefaultsToInfo() {
        
        String unknownLevel = "unknown";
        String expectedFormat = String.format(LoggingUtil.MSG_LOG, 
                                              LoggingUtil.MSG_LOG_PROJECT, 
                                              CLASS_NAME, 
                                              METHOD_NAME, 
                                              MESSAGE);

        LoggingUtil.log(logger, unknownLevel, CLASS_NAME, METHOD_NAME, MESSAGE);

        verify(logger).info(expectedFormat);
        verify(logger, never()).debug(anyString());
        verify(logger, never()).error(anyString());
        verify(logger, never()).warn(anyString());
        verify(logger, never()).trace(anyString());
    }

    @Test
    public void log_NullClassName_LogsWithNull() {
        
        String expectedFormat = String.format(LoggingUtil.MSG_LOG, 
                                              LoggingUtil.MSG_LOG_PROJECT, 
                                              "null", 
                                              METHOD_NAME, 
                                              MESSAGE);

        LoggingUtil.log(logger, LoggingUtil.INFO, null, METHOD_NAME, MESSAGE);

        verify(logger).info(expectedFormat);
    }

    @Test
    public void log_NullMethodName_LogsWithNull() {
        
        String expectedFormat = String.format(LoggingUtil.MSG_LOG, 
                                              LoggingUtil.MSG_LOG_PROJECT, 
                                              CLASS_NAME, 
                                              "null", 
                                              MESSAGE);

        LoggingUtil.log(logger, LoggingUtil.INFO, CLASS_NAME, null, MESSAGE);

        verify(logger).info(expectedFormat);
    }

    @Test
    public void log_NullMessage_LogsWithNull() {
        
        String expectedFormat = String.format(LoggingUtil.MSG_LOG, 
                                              LoggingUtil.MSG_LOG_PROJECT, 
                                              CLASS_NAME, 
                                              METHOD_NAME, 
                                              "null");

        LoggingUtil.log(logger, LoggingUtil.INFO, CLASS_NAME, METHOD_NAME, null);

        verify(logger).info(expectedFormat);
    }

    @Test
    public void log_EmptyMessage_LogsEmptyString() {
        
        String expectedFormat = String.format(LoggingUtil.MSG_LOG, 
                                              LoggingUtil.MSG_LOG_PROJECT, 
                                              CLASS_NAME, 
                                              METHOD_NAME, 
                                              "");

        LoggingUtil.log(logger, LoggingUtil.INFO, CLASS_NAME, METHOD_NAME, "");

        verify(logger).info(expectedFormat);
    }

    @Test
    public void log_EmptyLevel_UsesDefaultInfo() {
        
        String expectedFormat = String.format(LoggingUtil.MSG_LOG, 
                                              LoggingUtil.MSG_LOG_PROJECT,
                                              CLASS_NAME,
                                              METHOD_NAME,
                                              MESSAGE);

        LoggingUtil.log(logger, "", CLASS_NAME, METHOD_NAME, MESSAGE);

        // An empty string does not match any case; use the default (info)
        verify(logger).info(expectedFormat);
    }
}