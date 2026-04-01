package com.econocom.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String TEST_SECRET = "test-secret-key-for-jwt-unit-tests-must-be-32-bytes-long";
    private static final long TEST_EXPIRATION = 3600000L;      // 1 hour
    private static final long TEST_REFRESH_EXPIRATION = 86400000L;  // 24 hours
    private static final String TEST_EMAIL = "test@econocom.com";

    private SecretKey signingKey;

    @Before
    public void setUp() {
        
        jwtUtil = new JwtUtil();
        
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", TEST_EXPIRATION);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", TEST_REFRESH_EXPIRATION);
        
        signingKey = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void generateToken_ValidEmail_ReturnsValidToken() {
        
        String token = jwtUtil.generateToken(TEST_EMAIL);

        assertNotNull("Token should not be null", token);
        assertFalse("Token should not be empty", token.isEmpty());
        assertEquals("Token should have 3 parts (header.payload.signature)", 
                     3, token.split("\\.").length);
    }

    @Test
    public void generateToken_DifferentEmails_GeneratesDifferentTokens() {
        
        String email1 = "user1@econocom.com";
        String email2 = "user2@econocom.com";

        String token1 = jwtUtil.generateToken(email1);
        String token2 = jwtUtil.generateToken(email2);

        assertNotEquals("Different emails should generate different tokens", token1, token2);
    }

    @Test
    public void generateRefreshToken_ValidEmail_ReturnsValidRefreshToken() {
        
        String refreshToken = jwtUtil.generateRefreshToken(TEST_EMAIL);

        assertNotNull("Refresh token should not be null", refreshToken);
        assertFalse("Refresh token should not be empty", refreshToken.isEmpty());
        assertEquals("Refresh token should have 3 parts", 3, refreshToken.split("\\.").length);
    }

    @Test
    public void generateRefreshToken_HasLongerExpirationThanAccessToken() {
        
        String accessToken = jwtUtil.generateToken(TEST_EMAIL);
        String refreshToken = jwtUtil.generateRefreshToken(TEST_EMAIL);

        Date accessExpiration = jwtUtil.extractExpiration(accessToken);
        Date refreshExpiration = jwtUtil.extractExpiration(refreshToken);

        assertTrue("Refresh token should have longer expiration", refreshExpiration.after(accessExpiration));
    }

    @Test
    public void extractEmail_ValidToken_ReturnsEmail() {
        
        String token = jwtUtil.generateToken(TEST_EMAIL);

        String extractedEmail = jwtUtil.extractEmail(token);

        assertEquals("Extracted email should match original", TEST_EMAIL, extractedEmail);
    }

    @Test
    public void getEmailFromToken_ValidToken_ReturnsEmail() {
        
        String token = jwtUtil.generateToken(TEST_EMAIL);

        String extractedEmail = jwtUtil.getEmailFromToken(token);

        assertEquals("getEmailFromToken should return same email as extractEmail", 
                     jwtUtil.extractEmail(token), extractedEmail);
    }

    @Test
    public void extractExpiration_ValidToken_ReturnsExpirationDate() {
        
        String token = jwtUtil.generateToken(TEST_EMAIL);
        Date expectedExpiration = new Date(System.currentTimeMillis() + TEST_EXPIRATION);

        Date actualExpiration = jwtUtil.extractExpiration(token);

        // Allow 5 second tolerance for test execution time
        long tolerance = 5000;
        
        assertTrue("Expiration date should be within expected range",
                   Math.abs(actualExpiration.getTime() - expectedExpiration.getTime()) < tolerance);
    }

    @Test
    public void extractClaim_ValidToken_ReturnsClaim() {
        
        // Create token with custom claim
        Map<String, Object> claims = new HashMap<>();
        
        claims.put("role", "admin");
        claims.put("department", "IT");
        
        String token = Jwts.builder()
                           .setClaims(claims)
                           .setSubject(TEST_EMAIL)
                           .setIssuedAt(new Date(System.currentTimeMillis()))
                           .setExpiration(new Date(System.currentTimeMillis() + TEST_EXPIRATION))
                           .signWith(signingKey)
                           .compact();

        String role = jwtUtil.extractClaim(token, claimsMap -> (String) claimsMap.get("role"));
        String department = jwtUtil.extractClaim(token, claimsMap -> (String) claimsMap.get("department"));

        assertEquals("Role claim should be 'admin'", "admin", role);
        assertEquals("Department claim should be 'IT'", "IT", department);
    }

    @Test
    public void extractClaim_SubjectClaim_ReturnsSubject() {
        
        String token = jwtUtil.generateToken(TEST_EMAIL);

        String subject = jwtUtil.extractClaim(token, Claims::getSubject);

        assertEquals("Subject should be the email", TEST_EMAIL, subject);
    }

    @Test
    public void validateToken_ValidToken_ReturnsTrue() {
        
        String token = jwtUtil.generateToken(TEST_EMAIL);

        Boolean isValid = jwtUtil.validateToken(token);

        assertTrue("Valid token should return true", isValid);
    }

    @Test
    public void validateToken_ExpiredToken_ReturnsFalse() {
        
        String expiredToken = Jwts.builder()
                                  .setSubject(TEST_EMAIL)
                                  .setIssuedAt(new Date(System.currentTimeMillis() - 7200000))  // 2 hours ago
                                  .setExpiration(new Date(System.currentTimeMillis() - 3600000))  // 1 hour ago (expired)
                                  .signWith(signingKey)
                                  .compact();

        Boolean isValid = jwtUtil.validateToken(expiredToken);

        assertFalse("Expired token should return false", isValid);
    }

    @Test
    public void validateToken_InvalidToken_ReturnsFalse() {
        
        String invalidToken = "invalid.token.here";

        Boolean isValid = jwtUtil.validateToken(invalidToken);

        assertFalse("Invalid token should return false", isValid);
    }

    @Test
    public void validateToken_MalformedToken_ReturnsFalse() {
        
        String malformedToken = "not-a-jwt-token-at-all";

        Boolean isValid = jwtUtil.validateToken(malformedToken);

        assertFalse("Malformed token should return false", isValid);
    }

    @Test
    public void validateToken_TokenSignedWithDifferentKey_ReturnsFalse() {
        
        String differentSecret = "different-secret-key-for-testing-purposes-32";
        SecretKey differentKey = Keys.hmacShaKeyFor(differentSecret.getBytes(StandardCharsets.UTF_8));
        
        String tokenWithDifferentKey = Jwts.builder()
                                           .setSubject(TEST_EMAIL)
                                           .setIssuedAt(new Date(System.currentTimeMillis()))
                                           .setExpiration(new Date(System.currentTimeMillis() + TEST_EXPIRATION))
                                           .signWith(differentKey)
                                           .compact();

        Boolean isValid = jwtUtil.validateToken(tokenWithDifferentKey);

        assertFalse("Token signed with different key should return false", isValid);
    }

    // ==================== Token Refresh Tests ====================

    @Test
    public void refreshToken_ValidRefreshToken_ReturnsNewAccessToken() {
        
        String refreshToken = jwtUtil.generateRefreshToken(TEST_EMAIL);

        String newAccessToken = jwtUtil.refreshToken(refreshToken);

        assertNotNull("New access token should not be null", newAccessToken);
        assertFalse("New access token should not be empty", newAccessToken.isEmpty());
        assertEquals("New token should have same subject (email)", 
                     TEST_EMAIL, 
                     jwtUtil.extractEmail(newAccessToken));
    }

    @Test
    public void refreshToken_ValidRefreshToken_NewTokenHasAccessExpiration() {
        
        String refreshToken = jwtUtil.generateRefreshToken(TEST_EMAIL);

        String newAccessToken = jwtUtil.refreshToken(refreshToken);
        Date newExpiration = jwtUtil.extractExpiration(newAccessToken);
        Date expectedExpiration = new Date(System.currentTimeMillis() + TEST_EXPIRATION);

        // New token should have access token expiration (1 hour), not refresh token expiration (24 hours)
        long tolerance = 5000;
        assertTrue("Refreshed token should have access token expiration",
                   Math.abs(newExpiration.getTime() - expectedExpiration.getTime()) < tolerance);
    }

    @Test(expected = io.jsonwebtoken.MalformedJwtException.class)
    public void refreshToken_InvalidToken_ThrowsException() {
        
        String invalidToken = "invalid.refresh.token";

        jwtUtil.refreshToken(invalidToken);
    }

    @Test
    public void generateToken_NullEmail_GeneratesTokenWithNullSubject() {
        
        String nullEmail = null;

        String token = jwtUtil.generateToken(nullEmail);

        assertNotNull("Token should be generated even with null email", token);
    }

    /**
     * Test: generateToken_EmptyEmail_GeneratesTokenWithEmptySubject
     * Verifies behavior when email is empty string
     */
    @Test
    public void generateToken_EmptyEmail_GeneratesTokenWithEmptySubject() {
        
        String emptyEmail = "";

        String token = jwtUtil.generateToken(emptyEmail);

        assertNotNull("Token should be generated even with empty email", token);
    }

    @Test
    public void extractEmail_TokenWithEmptySubject_ReturnsEmptyString() {
        
        String token = jwtUtil.generateToken("");

        String extractedEmail = jwtUtil.extractEmail(token);

        assertEquals("Extracted email should be empty string", "", extractedEmail);
    }

    @Test
    public void extractExpiration_TokenHasCorrectExpiration() {
        
        long customExpiration = 1800000L;  // 30 minutes
        ReflectionTestUtils.setField(jwtUtil, "expiration", customExpiration);
        String token = jwtUtil.generateToken(TEST_EMAIL);

        Date expiration = jwtUtil.extractExpiration(token);
        Date issuedAt = jwtUtil.extractClaim(token, Claims::getIssuedAt);

        long actualDuration = expiration.getTime() - issuedAt.getTime();
        assertEquals("Token duration should match configured expiration", customExpiration, actualDuration);
    }
}
