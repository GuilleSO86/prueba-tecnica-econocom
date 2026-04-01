package com.econocom.auth.exception;

import com.econocom.auth.model.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class GlobalExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String LOGIN_URL = "/api/auth/login";
    private static final String SSO_CALLBACK_URL = "/api/auth/sso/callback";

    @Test
    public void handleInvalidCredentials_Returns401() throws Exception {
        
        LoginRequest request = new LoginRequest();
        
        request.setEmail("nonexistent@test.com");
        request.setPassword("wrongpassword");

        MvcResult result = mockMvc.perform(post(LOGIN_URL)
                                  .contentType(MediaType.APPLICATION_JSON)
                                  .content(objectMapper.writeValueAsString(request)))
                                  .andExpect(status().isUnauthorized())
                                  .andExpect(jsonPath("$.status").value(401))
                                  .andExpect(jsonPath("$.message").exists())
                                  .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        
        assertTrue(responseBody.contains("\"status\""));
        assertTrue(responseBody.contains("\"message\""));
        assertTrue(responseBody.contains("\"timestamp\""));
    }

    @Test
    public void handleInvalidCredentials_ResponseContainsTimestamp() throws Exception {
        
        LoginRequest request = new LoginRequest();
        
        request.setEmail("test@test.com");
        request.setPassword("wrongpassword");

        mockMvc.perform(post(LOGIN_URL)
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("$.timestamp").exists())
               .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    public void handleInvalidCredentials_ContentTypeIsJson() throws Exception {
        
        LoginRequest request = new LoginRequest();
        
        request.setEmail("test@test.com");
        request.setPassword("wrongpassword");

        mockMvc.perform(post(LOGIN_URL)
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isUnauthorized())
               .andExpect(result -> {
                    String contentType = result.getResponse().getContentType();
                    assertTrue(contentType.contains("application/json"));
                });
    }

    @Test
    public void handleIllegalArgument_Returns400() throws Exception {
        
        mockMvc.perform(get(SSO_CALLBACK_URL)
               .param("code", "invalid-code-not-simulated"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.status").value(400))
               .andExpect(jsonPath("$.error").value("Bad Request"))
               .andExpect(jsonPath("$.message").exists())
               .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    public void handleIllegalArgument_MissingCode_Returns400() throws Exception {
        
        mockMvc.perform(get(SSO_CALLBACK_URL))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.status").value(400))
               .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    public void handleIllegalArgument_ResponseStructure() throws Exception {
        
        MvcResult result = mockMvc.perform(get(SSO_CALLBACK_URL)
                                   .param("code", "invalid"))
                                   .andExpect(status().isBadRequest())
                                   .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        
        assertTrue(responseBody.contains("\"timestamp\""));
        assertTrue(responseBody.contains("\"status\""));
        assertTrue(responseBody.contains("\"error\""));
        assertTrue(responseBody.contains("\"message\""));
    }

    @Test
    public void errorResponse_AuthenticationExceptions_UseErrorResponseFormat() throws Exception {
        
        LoginRequest request = new LoginRequest();
        
        request.setEmail("test@test.com");
        request.setPassword("wrong");

        MvcResult result = mockMvc.perform(post(LOGIN_URL)
                                   .contentType(MediaType.APPLICATION_JSON)
                                   .content(objectMapper.writeValueAsString(request)))
                                   .andExpect(status().isUnauthorized())
                                   .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        
        assertTrue(responseBody.contains("\"status\""));
        assertTrue(responseBody.contains("\"message\""));
        assertTrue(responseBody.contains("\"timestamp\""));
        assertFalse(responseBody.contains("\"error\""));
    }

    @Test
    public void errorResponse_IllegalArgumentExceptions_UseMapFormat() throws Exception {
        
        MvcResult result = mockMvc.perform(get(SSO_CALLBACK_URL)
                                  .param("code", "invalid-code"))
                                  .andExpect(status().isBadRequest())
                                  .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        
        assertTrue(responseBody.contains("\"timestamp\""));
        assertTrue(responseBody.contains("\"status\""));
        assertTrue(responseBody.contains("\"error\""));
        assertTrue(responseBody.contains("\"message\""));
    }
}