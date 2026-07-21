package com.quizhub.authservice.repository;

import com.quizhub.authservice.entity.auth.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository <RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);
}
