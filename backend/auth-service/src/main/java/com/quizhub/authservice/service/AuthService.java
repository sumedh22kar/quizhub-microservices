package com.quizhub.authservice.service;

import com.quizhub.authservice.dto.request.LoginRequest;
import com.quizhub.authservice.dto.request.LogoutRequest;
import com.quizhub.authservice.dto.request.RefreshTokenRequest;
import com.quizhub.authservice.dto.request.RegisterRequest;
import com.quizhub.authservice.dto.response.LoginResponse;
import com.quizhub.authservice.dto.response.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    LoginResponse refreshToken(
            RefreshTokenRequest request
    );
    void logout(LogoutRequest request);

    void logoutAll();
}