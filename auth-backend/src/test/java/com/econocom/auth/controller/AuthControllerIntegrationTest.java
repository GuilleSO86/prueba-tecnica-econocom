package com.econocom.auth.controller;

import com.econocom.auth.model.LoginRequest;
import com.econocom.auth.model.LoginResponse;
import com.econocom.auth.model.Usuario;
import com.econocom.auth.repository.UsuarioRepository;
import com.econocom.auth.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_EMAIL = "integration@test.com";
    private static final String TEST_PASSWORD = "testpass123";
    private static final String TEST_NAME = "Integration Test User";

    @Before
    public void setUp() {

        // Delete the user if they already exist (to avoid duplicates)
        usuarioRepository.findByEmail(TEST_EMAIL)
                         .ifPresent(u -> usuarioRepository.delete(u));
        
        
        Usuario usuario = new Usuario();

        usuario.setEmail(TEST_EMAIL);
        usuario.setPassword(TEST_PASSWORD);
        usuario.setNombre(TEST_NAME);
        usuario.setActivo(true);
        
        usuarioRepository.save(usuario);
    }

    @Test
    public void login_ValidCredentials_Returns200WithTokens() throws Exception {
        
        LoginRequest request = new LoginRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                                  .contentType(MediaType.APPLICATION_JSON)
                                  .content(objectMapper.writeValueAsString(request)))
                                  .andExpect(status().isOk())
                                  .andExpect(jsonPath("$.token").exists())
                                  .andExpect(jsonPath("$.refreshToken").exists())
                                  .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        LoginResponse response = objectMapper.readValue(responseBody, LoginResponse.class);
        
        assertNotNull(response.getToken());
        assertNotNull(response.getRefreshToken());
        
        String emailFromToken = jwtUtil.extractEmail(response.getToken());
        assertEquals(TEST_EMAIL, emailFromToken);
    }

    @Test
    public void login_InvalidCredentials_Returns401() throws Exception {
        
        LoginRequest request = new LoginRequest();
        
        request.setEmail(TEST_EMAIL);
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isUnauthorized());
    }

    @Test
    public void login_MissingFields_Returns400() throws Exception {
        
        String emptyJson = "{\"email\":\"\",\"password\":\"\"}";

        mockMvc.perform(post("/api/auth/login")
               .contentType(MediaType.APPLICATION_JSON)
               .content(emptyJson))
               .andExpect(status().isUnauthorized());
    }

    @Test
    public void login_NonExistingEmail_Returns401() throws Exception {
        
        LoginRequest request = new LoginRequest();
        
        request.setEmail("nonexistent@test.com");
        request.setPassword(TEST_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isUnauthorized());
    }

    @Test
    public void login_InactiveUser_Returns401() throws Exception {
        
        String inactiveEmail = "inactive@test.com";
        usuarioRepository.findByEmail(inactiveEmail)
                         .ifPresent(u -> usuarioRepository.delete(u));
        
        Usuario inactiveUser = new Usuario();
        
        inactiveUser.setEmail(inactiveEmail);
        inactiveUser.setPassword(TEST_PASSWORD);
        inactiveUser.setNombre("Inactive User");
        inactiveUser.setActivo(false);
        
        usuarioRepository.save(inactiveUser);

        LoginRequest request = new LoginRequest();
        
        request.setEmail(inactiveEmail);
        request.setPassword(TEST_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isUnauthorized());
    }

    @Test
    public void initiateSso_ReturnsSsoUrl() throws Exception {
        
        mockMvc.perform(get("/api/auth/sso"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.ssoUrl").exists())
               .andExpect(jsonPath("$.ssoUrl").value(org.hamcrest.Matchers.containsString("/api/auth/sso/callback")))
               .andExpect(jsonPath("$.ssoUrl").value(org.hamcrest.Matchers.containsString("state=")));
    }

    @Test
    public void handleSsoCallback_ValidCode_ReturnsTokens() throws Exception {
        
        String ssoEmail = "sso@econocom.com";
        
        usuarioRepository.findByEmail(ssoEmail).ifPresent(u -> usuarioRepository.delete(u));
        
        usuarioRepository.flush();
        
        Usuario ssoUser = new Usuario();
        
        ssoUser.setEmail(ssoEmail);
        ssoUser.setPassword("sso-password");
        ssoUser.setNombre("SSO User");
        ssoUser.setActivo(true);
        
        usuarioRepository.save(ssoUser);
        usuarioRepository.flush();

        String validCode = "simulated-sso-code-123";
        String state = "test-state-123";

        MvcResult result = mockMvc.perform(get("/api/auth/sso/callback")
                                  .param("code", validCode)
                                  .param("state", state))
                                  .andExpect(status().isOk())
                                  .andExpect(jsonPath("$.token").exists())
                                  .andExpect(jsonPath("$.refreshToken").exists())
                                  .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        LoginResponse response = objectMapper.readValue(responseBody, LoginResponse.class);
        String emailFromToken = jwtUtil.extractEmail(response.getToken());
        
        assertEquals(ssoEmail, emailFromToken);
    }

    @Test
    public void handleSsoCallback_MissingCode_Returns400() throws Exception {
        
        mockMvc.perform(get("/api/auth/sso/callback")
               .param("state", "test-state"))
               .andExpect(status().isBadRequest());
    }

    @Test
    public void handleSsoCallback_InvalidCode_Returns400() throws Exception {
        
        mockMvc.perform(get("/api/auth/sso/callback")
               .param("code", "invalid-code")
               .param("state", "test-state"))
               .andExpect(status().isBadRequest());
    }

    @Test
    public void handleSsoCallback_SsoUserNotConfigured_Returns401() throws Exception {
        
        String ssoEmail = "sso@econocom.com";
        
        usuarioRepository.findByEmail(ssoEmail)
                         .ifPresent(u -> usuarioRepository.delete(u));

        mockMvc.perform(get("/api/auth/sso/callback")
               .param("code", "simulated-sso-code-123")
               .param("state", "test-state"))
               .andExpect(status().isUnauthorized());
    }
}