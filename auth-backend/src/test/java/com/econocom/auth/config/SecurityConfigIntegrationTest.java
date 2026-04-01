package com.econocom.auth.config;

import com.econocom.auth.util.JwtUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    private String validToken;

    @Before
    public void setUp() {
        
        validToken = jwtUtil.generateToken("test@security.com");
    }

    
    @Test
    public void publicEndpoints_NoAuth_Allowed() throws Exception {
        
        mockMvc.perform(post("/api/auth/login")
               .contentType(MediaType.APPLICATION_JSON)
               .content("{\"email\":\"test@test.com\",\"password\":\"123\"}"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    public void publicEndpoints_AuthLoginPath_Allowed() throws Exception {
        
        mockMvc.perform(get("/api/auth/sso"))
               .andExpect(status().isOk());
    }

    @Test
    public void protectedEndpoints_NoAuth_Returns403() throws Exception {
        
        mockMvc.perform(get("/api/unknown/protected"))
               .andExpect(status().isForbidden());
    }

    @Test
    public void protectedEndpoints_ValidToken_Allowed() throws Exception {
        
        mockMvc.perform(get("/api/unknown/protected")
               .header("Authorization", "Bearer " + validToken))
               .andExpect(status().isNotFound()); // 404 = Authentication successful, resource does not exist
    }

    @Test
    public void csrf_Disabled_PostWithoutCsrfToken_Allowed() throws Exception {
        
        // A POST request without a CSRF token should work (it is disabled)
        mockMvc.perform(post("/api/auth/login")
               .contentType(MediaType.APPLICATION_JSON)
               .content("{\"email\":\"test@test.com\",\"password\":\"123\"}"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    public void cors_Configured_OptionsRequestAllowed() throws Exception {
        
        // Preflight requests must be allowed
        mockMvc.perform(options("/api/auth/login")
               .header("Origin", "http://localhost:4200")
               .header("Access-Control-Request-Method", "POST"))
               .andExpect(status().isOk());
    }

    @Test
    public void cors_Configured_ActualRequestWithOriginAllowed() throws Exception {
        
        // Requests with an Origin header must be allowed
        mockMvc.perform(get("/api/auth/sso")
               .header("Origin", "http://localhost:4200"))
               .andExpect(status().isOk());
    }
}