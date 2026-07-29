package com.quizhub.authservice.security.jwt;

import com.quizhub.authservice.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final String secret;
    private final long expiration;

    public JwtService(@Value("${jwt.secret:VGhpc0lzQVN1cGVyU2VjdXJlSldUU2VjcmV0S2V5Rm9yUXVpekh1YkF1dGhTZXJ2aWNlMTIzNDU2Nzg5}") String secret,
                      @Value("${jwt.expiration:86400000}") long expiration) {
        this.secret = secret;
        this.expiration = expiration;
    }

    public String generateToken(UserDetails userDetails) {
        var builder = Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration));

        if (userDetails instanceof CustomUserDetails customUserDetails) {
            builder.claim("userId", customUserDetails.getUser().getId().toString());
            builder.claim("username", customUserDetails.getUser().getEmail());
            String roles = customUserDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));
            builder.claim("roles", roles);
        }

        return builder.signWith(getSigningKey()).compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername())
                && !extractExpiration(token).before(new Date());
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
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

    public long getExpiration() {
        return expiration;
    }
}