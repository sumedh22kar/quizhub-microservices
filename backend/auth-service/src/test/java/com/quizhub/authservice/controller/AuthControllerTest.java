package com.quizhub.authservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizhub.authservice.dto.request.LoginRequest;
import com.quizhub.authservice.dto.request.LogoutRequest;
import com.quizhub.authservice.dto.request.RefreshTokenRequest;
import com.quizhub.authservice.entity.auth.Role;
import com.quizhub.authservice.entity.auth.User;
import com.quizhub.authservice.repository.RoleRepository;
import com.quizhub.authservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private final String testEmail = "testuser@example.com";
    private final String testPassword = "Password123!";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        Role userRole = roleRepository.findByName(com.quizhub.authservice.entity.auth.RoleType.ROLE_USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(com.quizhub.authservice.entity.auth.RoleType.ROLE_USER).build()));

        User user = User.builder()
                .firstName("Test")
                .lastName("User")
                .email(testEmail)
                .password(passwordEncoder.encode(testPassword))
                .enabled(true)
                .roles(Set.of(userRole))
                .build();

        userRepository.save(user);
    }

    @Test
    void testLogin_Success() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email(testEmail)
                .password(testPassword)
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    void testRefreshToken_Success() throws Exception {
        // Step 1: Login to get refresh token
        LoginRequest loginRequest = LoginRequest.builder()
                .email(testEmail)
                .password(testPassword)
                .build();

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginResponseBody = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String refreshToken = loginResponseBody.get("data").get("refreshToken").asText();

        // Step 2: Call Refresh Token API
        RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Token refreshed successfully"))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.refreshToken", notNullValue()));
    }

    @Test
    void testLogout_Success() throws Exception {
        // Step 1: Login to get refresh token
        LoginRequest loginRequest = LoginRequest.builder()
                .email(testEmail)
                .password(testPassword)
                .build();

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginResponseBody = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String refreshToken = loginResponseBody.get("data").get("refreshToken").asText();

        // Step 2: Logout using refresh token
        LogoutRequest logoutRequest = LogoutRequest.builder()
                .refreshToken(refreshToken)
                .build();

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }

    @Test
    void testLogoutAll_Success() throws Exception {
        // Step 1: Login to get access token
        LoginRequest loginRequest = LoginRequest.builder()
                .email(testEmail)
                .password(testPassword)
                .build();

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginResponseBody = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String accessToken = loginResponseBody.get("data").get("accessToken").asText();

        // Step 2: Logout All devices with Authorization Header
        mockMvc.perform(post("/api/v1/auth/logout-all")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logged out from all devices"));
    }

    @Test
    void testLogoutAll_Unauthorized() throws Exception {
        // Call logout-all without bearer token
        mockMvc.perform(post("/api/v1/auth/logout-all"))
                .andExpect(status().isUnauthorized());
    }
}
