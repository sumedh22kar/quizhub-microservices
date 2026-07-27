package com.quizhub.authservice.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = "VGhpc0lzQVN1cGVyU2VjdXJlSldUU2VjcmV0S2V5Rm9yUXVpekh1YkF1dGhTZXJ2aWNlMTIzNDU2Nzg5";
    private final long expiration = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(secret, expiration);
    }

    @Test
    void testGenerateAndValidateToken() {
        UserDetails userDetails = new User("test@example.com", "password", Collections.emptyList());

        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        String extractedUsername = jwtService.extractUsername(token);
        assertEquals("test@example.com", extractedUsername);

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void testIsTokenValid_WrongUser() {
        UserDetails userDetails1 = new User("test1@example.com", "password", Collections.emptyList());
        UserDetails userDetails2 = new User("test2@example.com", "password", Collections.emptyList());

        String token = jwtService.generateToken(userDetails1);

        assertFalse(jwtService.isTokenValid(token, userDetails2));
    }

    @Test
    void testExtractExpiration() {
        UserDetails userDetails = new User("test@example.com", "password", Collections.emptyList());

        String token = jwtService.generateToken(userDetails);
        Date expirationDate = jwtService.extractExpiration(token);

        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()));
    }
}
