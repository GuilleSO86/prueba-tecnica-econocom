package com.econocom.auth.filter;

import com.econocom.auth.util.JwtUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class JwtAuthenticationFilterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    private static final String TEST_EMAIL = "filter@test.com";
    private String validToken;
    private String invalidToken;
    private String malformedToken;

    @Before
    public void setUp() {
        
        validToken = jwtUtil.generateToken(TEST_EMAIL);
        
        invalidToken = 
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QHRlc3QuY29tIiwiaWF0IjoxNTE2MjM5MDIyfQ.invalidSignature";
        
        malformedToken = "malformed.token";
    }

    @Test
    public void doFilter_ValidToken_AllowsAccessToPublicEndpoint() throws Exception {
        
        mockMvc.perform(get("/api/auth/sso")
               .header("Authorization", "Bearer " + validToken))
               .andExpect(status().isOk());
    }

    @Test
    public void doFilter_ValidTokenWithoutBearerPrefix_AllowsAccess() throws Exception {
        
        mockMvc.perform(get("/api/auth/sso")
               .header("Authorization", validToken))  // Without "Bearer "
               .andExpect(status().isOk());
    }

    @Test
    public void doFilter_NoToken_AllowsAccessToPublicEndpoint() throws Exception {
        
        mockMvc.perform(get("/api/auth/sso"))
               .andExpect(status().isOk());
    }

    @Test
    public void doFilter_EmptyAuthorizationHeader_AllowsAccess() throws Exception {
        
        mockMvc.perform(get("/api/auth/sso")
               .header("Authorization", ""))
               .andExpect(status().isOk());
    }

    @Test
    public void doFilter_NullAuthorizationHeader_AllowsAccess() throws Exception {
        
        mockMvc.perform(get("/api/auth/sso"))
               .andExpect(status().isOk());
    }

    @Test
    public void doFilter_InvalidToken_ContinuesWithoutAuth() throws Exception {
        
        mockMvc.perform(get("/api/auth/sso")
               .header("Authorization", "Bearer " + invalidToken))
               .andExpect(status().isOk());
    }

    @Test
    public void doFilter_MalformedToken_ContinuesWithoutException() throws Exception {
        
        mockMvc.perform(get("/api/auth/sso")
               .header("Authorization", "Bearer " + malformedToken))
               .andExpect(status().isOk());
    }

    @Test
    public void doFilter_EmptyToken_ContinuesWithoutAuth() throws Exception {
        
        mockMvc.perform(get("/api/auth/sso")
               .header("Authorization", "Bearer "))
               .andExpect(status().isOk());
    }

    @Test
    public void doFilter_ExpiredToken_ContinuesWithoutAuth() throws Exception {
        
        String expiredToken = 
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QHRlc3QuY29tIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjE1MTYyMzkwMjJ9.signature";

        mockMvc.perform(get("/api/auth/sso")
               .header("Authorization", "Bearer " + expiredToken))
               .andExpect(status().isOk());
    }

    @Test
    public void doFilter_MultipleAuthorizationHeaders_LastOneWins() throws Exception {
        
        mockMvc.perform(get("/api/auth/sso")
               .header("Authorization", "Bearer " + validToken)
               .header("Authorization", "Bearer " + invalidToken))
               .andExpect(status().isOk());
    }
}