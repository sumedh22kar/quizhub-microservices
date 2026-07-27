package com.quizhub.authservice.service.impl;

import com.quizhub.authservice.entity.auth.RefreshToken;
import com.quizhub.authservice.entity.auth.User;
import com.quizhub.authservice.exception.InvalidRefreshTokenException;
import com.quizhub.authservice.exception.RefreshTokenExpiredException;
import com.quizhub.authservice.repository.RefreshTokenRepository;
import com.quizhub.authservice.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl
        implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long expiration;

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository,
                                  @Value("${refresh-token.expiration:604800000}") long expiration) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.expiration = expiration;
    }

    @Override
    public RefreshToken createRefreshToken(User user) {

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(
                        Instant.now()
                                .plusMillis(expiration)
                )
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken verifyRefreshToken(String token) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(token)
                .orElseThrow(() ->
                        new InvalidRefreshTokenException(
                                "Refresh token not found."
                        ));

        if (refreshToken.isRevoked()) {
            throw new InvalidRefreshTokenException(
                    "Refresh token has been revoked."
            );
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {

            refreshTokenRepository.delete(refreshToken);

            throw new RefreshTokenExpiredException(
                    "Refresh token has expired."
            );
        }

        return refreshToken;
    }

    @Override
    public void revokeRefreshToken(String token) {

        RefreshToken refreshToken =
                refreshTokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new InvalidRefreshTokenException(
                                        "Refresh token not found."
                                ));

        refreshToken.setRevoked(true);

        refreshTokenRepository.save(refreshToken);
    }

    @Override
    public void revokeAllUserTokens(User user) {

        List<RefreshToken> tokens =
                refreshTokenRepository.findAllByUser(user);

        tokens.forEach(token -> token.setRevoked(true));

        refreshTokenRepository.saveAll(tokens);
    }
}
