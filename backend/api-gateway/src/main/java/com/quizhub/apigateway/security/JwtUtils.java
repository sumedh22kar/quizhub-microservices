package com.quizhub.apigateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${jwt.secret:VGhpc0lzQVN1cGVyU2VjdXJlSldUU2VjcmV0S2V5Rm9yUXVpekh1YkF1dGhTZXJ2aWNlMTIzNDU2Nzg5}")
    private String secret;

    public boolean validateToken(String token) {
        try {
            Claims claims = getAllClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public String getUserIdFromToken(String token) {
        Claims claims = getAllClaims(token);
        String userId = claims.get("userId", String.class);
        if (userId != null) {
            return userId;
        }
        return claims.getSubject();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = getAllClaims(token);
        String username = claims.get("username", String.class);
        if (username != null) {
            return username;
        }
        return claims.getSubject();
    }

    public String getRolesFromToken(String token) {
        Claims claims = getAllClaims(token);
        return claims.get("roles", String.class);
    }

    private Claims getAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
