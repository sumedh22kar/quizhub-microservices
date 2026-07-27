package com.quizhub.authservice.service;

import com.quizhub.authservice.entity.auth.RefreshToken;
import com.quizhub.authservice.entity.auth.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);

    RefreshToken verifyRefreshToken(String token);

    void revokeRefreshToken(String token);

    void revokeAllUserTokens(User user);

}