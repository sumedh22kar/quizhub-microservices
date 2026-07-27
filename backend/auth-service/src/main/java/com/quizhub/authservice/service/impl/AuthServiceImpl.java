package com.quizhub.authservice.service.impl;

import com.quizhub.authservice.dto.request.LoginRequest;
import com.quizhub.authservice.dto.request.LogoutRequest;
import com.quizhub.authservice.dto.request.RefreshTokenRequest;
import com.quizhub.authservice.dto.request.RegisterRequest;
import com.quizhub.authservice.dto.response.LoginResponse;
import com.quizhub.authservice.dto.response.RegisterResponse;
import com.quizhub.authservice.entity.auth.RefreshToken;
import com.quizhub.authservice.entity.auth.Role;
import com.quizhub.authservice.entity.auth.RoleType;
import com.quizhub.authservice.entity.auth.User;
import com.quizhub.authservice.exception.DuplicateResourceException;
import com.quizhub.authservice.exception.ResourceNotFoundException;
import com.quizhub.authservice.repository.RoleRepository;
import com.quizhub.authservice.repository.UserRepository;
import com.quizhub.authservice.security.CustomUserDetailsService;


import com.quizhub.authservice.security.jwt.JwtService;
import com.quizhub.authservice.service.AuthService;
import com.quizhub.authservice.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;

    private final RefreshTokenService refreshTokenService;


    @Override
    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists.");
        }

        Role role = roleRepository.findByName(RoleType.ROLE_USER)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Default role ROLE_USER not found."));

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(encodedPassword)
                .enabled(true)
                .build();

        user.getRoles().add(role);

        User savedUser = userRepository.save(user);

        return RegisterResponse.builder()
                .id(savedUser.getId())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .email(savedUser.getEmail())
                .build();
    }
    @Override
    public LoginResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetails userDetails =
                customUserDetailsService.loadUserByUsername(request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user);

        String accessToken =
                jwtService.generateToken(userDetails);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpiration())
                .build();
    }

    @Override
    public LoginResponse refreshToken(RefreshTokenRequest request) {

        // Step 1 - Verify refresh token
        RefreshToken refreshToken =
                refreshTokenService.verifyRefreshToken(request.getRefreshToken());

        // Step 2 - Get user
        User user = refreshToken.getUser();

        // Step 3 - Load UserDetails
        UserDetails userDetails =
                customUserDetailsService.loadUserByUsername(user.getEmail());

        // Step 4 - Generate new access token
        String accessToken =
                jwtService.generateToken(userDetails);

        // Step 5 - Rotate refresh token
        refreshTokenService.revokeRefreshToken(refreshToken.getToken());

        RefreshToken newRefreshToken =
                refreshTokenService.createRefreshToken(user);

        // Step 6 - Return response
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpiration())
                .build();
    }
    @Override
    public void logout(LogoutRequest request) {

        refreshTokenService.verifyRefreshToken(
                request.getRefreshToken()
        );

        refreshTokenService.revokeRefreshToken(
                request.getRefreshToken()
        );
    }
    @Override
    public void logoutAll() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        refreshTokenService.revokeAllUserTokens(user);
    }
}