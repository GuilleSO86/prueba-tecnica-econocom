package com.econocom.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    
    /**
     * Retrieves the signing key used for JWT token operations.
     *
     * @return the SecretKey used for HMAC-SHA signing
     */
    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Extracts the email (subject) from a JWT token.
     *
     * @param token the JWT token
     * 
     * @return the email address stored as the token subject
     */
    public String extractEmail(String token) {

        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from a JWT token.
     *
     * @param token the JWT token
     * 
     * @return the expiration Date of the token
     */
    public Date extractExpiration(String token) {

        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from a JWT token using a resolver function.
     *
     * @param <T> the type of the claim to extract
     * @param token the JWT token
     * @param claimsResolver function to extract the desired claim from Claims
     * 
     * @return the extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {

        final Claims claims = extractAllClaims(token);

        return claimsResolver.apply(claims);
    }

    /**
     * Parses and extracts all claims from a JWT token.
     *
     * @param token the JWT token
     * 
     * @return the Claims object containing all token claims
     */
    private Claims extractAllClaims(String token) {

        return Jwts.parserBuilder()
                   .setSigningKey(getSigningKey())
                   .build()
                   .parseClaimsJws(token)
                   .getBody();
    }

    /**
     * Checks if a JWT token has expired.
     *
     * @param token the JWT token
     * 
     * @return true if the token is expired, false otherwise
     */
    private Boolean isTokenExpired(String token) {

        return extractExpiration(token).before(new Date());
    }

    /**
     * Generates a JWT access token for the given email.
     *
     * @param email the email address to use as the token subject
     * 
     * @return the signed JWT access token
     */
    public String generateToken(String email) {

        Map<String, Object> claims = new HashMap<>();

        return createToken(claims, email, expiration);
    }

    /**
     * Generates a JWT refresh token for the given email.
     * Refresh tokens typically have a longer expiration than access tokens.
     *
     * @param email the email address to use as the token subject
     * 
     * @return the signed JWT refresh token
     */
    public String generateRefreshToken(String email) {

        Map<String, Object> claims = new HashMap<>();

        return createToken(claims, email, refreshExpiration);
    }

    /**
     * Creates a JWT token with the specified claims, subject, and expiration.
     *
     * @param claims the claims to include in the token
     * @param subject the subject (typically email) for the token
     * @param expirationTime the expiration time in milliseconds
     * 
     * @return the signed JWT token string
     */
    private String createToken(Map<String, Object> claims, String subject, long expirationTime) {

        return Jwts.builder()
                   .setClaims(claims)
                   .setSubject(subject)
                   .setIssuedAt(new Date(System.currentTimeMillis()))
                   .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                   .signWith(getSigningKey())
                   .compact();
    }

    /**
     * Validates a JWT token by checking if it is not expired.
     *
     * @param token the JWT token to validate
     * 
     * @return true if the token is valid (not expired), false otherwise
     */
    public Boolean validateToken(String token) {

        try {
            return !isTokenExpired(token);

        } catch (Exception e) {

            return false;
        }
    }

    /**
     * Extracts the email from a JWT token.
     * This is a convenience method that delegates to extractEmail.
     *
     * @param token the JWT token
     * 
     * @return the email address from the token
     */
    public String getEmailFromToken(String token) {

        return extractEmail(token);
    }

    /**
     * Refreshes a JWT token by generating a new access token from an existing refresh token.
     *
     * @param token the refresh token to use for generating a new access token
     * 
     * @return a new JWT access token with updated expiration
     * 
     * @throws Exception if the token is invalid or expired
     */
    public String refreshToken(String token) {

        final Claims claims = extractAllClaims(token);

        String email = claims.getSubject();
        
        return generateToken(email);
    }
}
