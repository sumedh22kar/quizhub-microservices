package com.quizhub.authservice.service;

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
import com.quizhub.authservice.repository.RoleRepository;
import com.quizhub.authservice.repository.UserRepository;
import com.quizhub.authservice.security.CustomUserDetailsService;
import com.quizhub.authservice.security.jwt.JwtService;
import com.quizhub.authservice.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = Role.builder().name(RoleType.ROLE_USER).build();

        testUser = User.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .password("encoded_pass")
                .enabled(true)
                .build();
        testUser.setId(UUID.randomUUID());
    }

    @Test
    void testRegister_Success() {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .password("Password123!")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(roleRepository.findByName(RoleType.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        RegisterResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("jane.doe@example.com", response.getEmail());
        assertEquals("Jane", response.getFirstName());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegister_DuplicateEmail() {
        RegisterRequest request = RegisterRequest.builder()
                .email("jane.doe@example.com")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testLogin_Success() {
        LoginRequest request = LoginRequest.builder()
                .email("jane.doe@example.com")
                .password("Password123!")
                .build();

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                "jane.doe@example.com", "encoded_pass", Collections.emptyList());

        RefreshToken refreshToken = RefreshToken.builder().token("ref-token-123").user(testUser).build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(customUserDetailsService.loadUserByUsername(request.getEmail())).thenReturn(userDetails);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));
        when(refreshTokenService.createRefreshToken(testUser)).thenReturn(refreshToken);
        when(jwtService.generateToken(userDetails)).thenReturn("access-token-123");
        when(jwtService.getExpiration()).thenReturn(86400000L);

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("access-token-123", response.getAccessToken());
        assertEquals("ref-token-123", response.getRefreshToken());
    }

    @Test
    void testRefreshToken_Success() {
        RefreshTokenRequest request = RefreshTokenRequest.builder().refreshToken("old-ref-token").build();

        RefreshToken oldRefreshToken = RefreshToken.builder().token("old-ref-token").user(testUser).build();
        RefreshToken newRefreshToken = RefreshToken.builder().token("new-ref-token").user(testUser).build();

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                "jane.doe@example.com", "encoded_pass", Collections.emptyList());

        when(refreshTokenService.verifyRefreshToken("old-ref-token")).thenReturn(oldRefreshToken);
        when(customUserDetailsService.loadUserByUsername("jane.doe@example.com")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("new-access-token");
        when(refreshTokenService.createRefreshToken(testUser)).thenReturn(newRefreshToken);
        when(jwtService.getExpiration()).thenReturn(86400000L);

        LoginResponse response = authService.refreshToken(request);

        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-ref-token", response.getRefreshToken());

        verify(refreshTokenService, times(1)).revokeRefreshToken("old-ref-token");
    }

    @Test
    void testLogout_Success() {
        LogoutRequest request = LogoutRequest.builder().refreshToken("ref-token-logout").build();
        RefreshToken refreshToken = RefreshToken.builder().token("ref-token-logout").user(testUser).build();

        when(refreshTokenService.verifyRefreshToken("ref-token-logout")).thenReturn(refreshToken);

        authService.logout(request);

        verify(refreshTokenService, times(1)).revokeRefreshToken("ref-token-logout");
    }

    @Test
    void testLogoutAll_Success() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("jane.doe@example.com");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.of(testUser));

        authService.logoutAll();

        verify(refreshTokenService, times(1)).revokeAllUserTokens(testUser);
        SecurityContextHolder.clearContext();
    }
}
