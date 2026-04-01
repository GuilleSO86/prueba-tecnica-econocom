package com.econocom.auth;

import com.econocom.auth.model.LoginRequest;
import com.econocom.auth.model.LoginResponse;
import com.econocom.auth.model.Usuario;
import com.econocom.auth.repository.UsuarioRepository;
import com.econocom.auth.util.JwtUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthFlowEndToEndTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private static final String TEST_EMAIL = "e2e@test.com";
    private static final String TEST_PASSWORD = "e2epass123";
    private static final String TEST_NAME = "E2E Test User";
    private static final String BASE_URL = "/api/auth";

    @Before
    public void setUp() {
        
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
    public void completeLoginFlow_ValidCredentials_Success() {
        
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword(TEST_PASSWORD);

        ResponseEntity<LoginResponse> loginResponse = restTemplate.postForEntity(
                BASE_URL + "/login",
                loginRequest,
                LoginResponse.class
        );

        assertEquals(200, loginResponse.getStatusCodeValue());
        assertNotNull(loginResponse.getBody());
        assertNotNull(loginResponse.getBody().getToken());
        assertNotNull(loginResponse.getBody().getRefreshToken());

        String accessToken = loginResponse.getBody().getToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        
        String emailFromToken = jwtUtil.extractEmail(accessToken);
        
        assertEquals(TEST_EMAIL, emailFromToken);
    }

    @Test
    public void tokenRefreshFlow_ValidRefreshToken_NewAccessToken() {
        
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword(TEST_PASSWORD);

        ResponseEntity<LoginResponse> loginResponse = restTemplate.postForEntity(
                BASE_URL + "/login",
                loginRequest,
                LoginResponse.class
        );

        assertEquals(200, loginResponse.getStatusCodeValue());
        
        String refreshToken = loginResponse.getBody().getRefreshToken();

        assertTrue(jwtUtil.validateToken(refreshToken));
        
        String emailFromRefresh = jwtUtil.extractEmail(refreshToken);
        
        assertEquals(TEST_EMAIL, emailFromRefresh);
    }

    @Test
    public void ssoCompleteFlow_SimulatedSso_Success() {
        ResponseEntity<Map> ssoInitResponse = restTemplate.getForEntity(
                BASE_URL + "/sso",
                java.util.Map.class
        );

        assertEquals(200, ssoInitResponse.getStatusCodeValue());
        assertNotNull(ssoInitResponse.getBody());
        assertNotNull(ssoInitResponse.getBody().get("ssoUrl"));

        String ssoUrl = (String) ssoInitResponse.getBody().get("ssoUrl");

        assertTrue(ssoUrl.contains("code="));
        assertTrue(ssoUrl.contains("state="));

        String ssoEmail = "sso@econocom.com";
        usuarioRepository.findByEmail(ssoEmail).ifPresent(u -> usuarioRepository.delete(u));
        
        Usuario ssoUser = new Usuario();
        
        ssoUser.setEmail(ssoEmail);
        ssoUser.setPassword("sso-password");
        ssoUser.setNombre("SSO User");
        ssoUser.setActivo(true);
        
        usuarioRepository.save(ssoUser);

        String code = "simulated-sso-code-123";
        
        ResponseEntity<LoginResponse> callbackResponse = restTemplate.getForEntity(
                BASE_URL + "/sso/callback?code={code}&state={state}",
                LoginResponse.class,
                code,
                "test-state"
        );

        assertEquals(200, callbackResponse.getStatusCodeValue());
        assertNotNull(callbackResponse.getBody());
        assertNotNull(callbackResponse.getBody().getToken());
    }

    @Test
    public void accessProtectedWithoutToken_Returns401() {
        
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Attempting to access a protected endpoint without a token
        // As there is no specific protected endpoint, we use one that does not exist
        // but which requires authentication according to SecurityConfig (anyRequest().authenticated())
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/unknown/protected",
                HttpMethod.GET,
                entity,
                String.class
        );

        int statusCode = response.getStatusCodeValue();
        
        assertTrue("Expected 401 or 403 but was " + statusCode, statusCode == 401 || statusCode == 403);
    }

    @Test
    public void accessProtectedWithInvalidToken_Returns401() {
        
        HttpHeaders headers = new HttpHeaders();
        
        headers.set("Authorization", "Bearer invalid.token.here");
        
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/unknown/protected",
                HttpMethod.GET,
                entity,
                String.class
        );

        int statusCode = response.getStatusCodeValue();
        
        assertTrue("Expected 401 or 403 but was " + statusCode, statusCode == 401 || statusCode == 403);
    }

    @Test
    public void accessProtectedWithExpiredToken_Returns401() {
        
        // Token has expired (simulated)
        String expiredToken = 
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QHRlc3QuY29tIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjE1MTYyMzkwMjJ9.signature";
        
        HttpHeaders headers = new HttpHeaders();
        
        headers.set("Authorization", "Bearer " + expiredToken);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/unknown/protected",
                HttpMethod.GET,
                entity,
                String.class
        );

        int statusCode = response.getStatusCodeValue();
        
        assertTrue("Expected 401 or 403 but was " + statusCode, statusCode == 401 || statusCode == 403);
    }
}