package com.quizhub.authservice.service;

import com.quizhub.authservice.entity.auth.RefreshToken;
import com.quizhub.authservice.entity.auth.User;
import com.quizhub.authservice.exception.InvalidRefreshTokenException;
import com.quizhub.authservice.exception.RefreshTokenExpiredException;
import com.quizhub.authservice.repository.RefreshTokenRepository;
import com.quizhub.authservice.service.impl.RefreshTokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenServiceImpl refreshTokenService;

    private User testUser;
    private final long expiration = 604800000L;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenServiceImpl(refreshTokenRepository, expiration);

        testUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();
    }

    @Test
    void testCreateRefreshToken() {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(testUser)
                .expiresAt(Instant.now().plusMillis(expiration))
                .revoked(false)
                .build();

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        RefreshToken result = refreshTokenService.createRefreshToken(testUser);

        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertFalse(result.isRevoked());
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void testVerifyRefreshToken_Success() {
        String tokenStr = "valid-token-uuid";
        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenStr)
                .user(testUser)
                .expiresAt(Instant.now().plusMillis(100000))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(refreshToken));

        RefreshToken verified = refreshTokenService.verifyRefreshToken(tokenStr);

        assertNotNull(verified);
        assertEquals(tokenStr, verified.getToken());
    }

    @Test
    void testVerifyRefreshToken_NotFound() {
        when(refreshTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        assertThrows(InvalidRefreshTokenException.class, () ->
                refreshTokenService.verifyRefreshToken("invalid-token"));
    }

    @Test
    void testVerifyRefreshToken_Revoked() {
        String tokenStr = "revoked-token";
        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenStr)
                .user(testUser)
                .expiresAt(Instant.now().plusMillis(100000))
                .revoked(true)
                .build();

        when(refreshTokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(refreshToken));

        assertThrows(InvalidRefreshTokenException.class, () ->
                refreshTokenService.verifyRefreshToken(tokenStr));
    }

    @Test
    void testVerifyRefreshToken_Expired() {
        String tokenStr = "expired-token";
        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenStr)
                .user(testUser)
                .expiresAt(Instant.now().minusMillis(100000))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(refreshToken));

        assertThrows(RefreshTokenExpiredException.class, () ->
                refreshTokenService.verifyRefreshToken(tokenStr));

        verify(refreshTokenRepository, times(1)).delete(refreshToken);
    }

    @Test
    void testRevokeRefreshToken() {
        String tokenStr = "token-to-revoke";
        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenStr)
                .user(testUser)
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(refreshToken));

        refreshTokenService.revokeRefreshToken(tokenStr);

        assertTrue(refreshToken.isRevoked());
        verify(refreshTokenRepository, times(1)).save(refreshToken);
    }

    @Test
    void testRevokeAllUserTokens() {
        RefreshToken token1 = RefreshToken.builder().token("t1").user(testUser).revoked(false).build();
        RefreshToken token2 = RefreshToken.builder().token("t2").user(testUser).revoked(false).build();

        when(refreshTokenRepository.findAllByUser(testUser)).thenReturn(List.of(token1, token2));

        refreshTokenService.revokeAllUserTokens(testUser);

        assertTrue(token1.isRevoked());
        assertTrue(token2.isRevoked());
        verify(refreshTokenRepository, times(1)).saveAll(any());
    }
}
