package com.quizhub.authservice.service.impl;

import com.quizhub.authservice.dto.request.LoginRequest;
import com.quizhub.authservice.dto.request.RegisterRequest;
import com.quizhub.authservice.dto.response.LoginResponse;
import com.quizhub.authservice.dto.response.RegisterResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

        String token = jwtService.generateToken(userDetails);

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpiration())
                .build();
    }
}