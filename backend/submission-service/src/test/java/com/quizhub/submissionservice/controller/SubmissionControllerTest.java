package com.quizhub.submissionservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizhub.submissionservice.dto.request.StartSubmissionRequest;
import com.quizhub.submissionservice.dto.request.SubmitAnswerRequest;
import com.quizhub.submissionservice.dto.request.SubmitQuizRequest;
import com.quizhub.submissionservice.dto.response.SubmissionResponse;
import com.quizhub.submissionservice.entity.enums.SubmissionStatus;
import com.quizhub.submissionservice.service.SubmissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubmissionController.class)
class SubmissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SubmissionService submissionService;

    @MockitoBean
    private com.quizhub.submissionservice.security.UserContext userContext;

    private UUID userId;
    private UUID quizId;
    private UUID submissionId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        quizId = UUID.randomUUID();
        submissionId = UUID.randomUUID();
    }

    @Test
    @DisplayName("POST /api/v1/submissions/start should start submission")
    void testStartQuiz() throws Exception {
        StartSubmissionRequest request = StartSubmissionRequest.builder()
                .quizId(quizId)
                .build();

        SubmissionResponse response = SubmissionResponse.builder()
                .id(submissionId)
                .quizId(quizId)
                .userId(userId)
                .status(SubmissionStatus.IN_PROGRESS)
                .startedAt(Instant.now())
                .build();

        when(submissionService.startQuiz(any(), eq(userId))).thenReturn(response);

        mockMvc.perform(post("/api/v1/submissions/start")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(submissionId.toString()))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("POST /api/v1/submissions/submit should submit quiz")
    void testSubmitQuiz() throws Exception {
        SubmitQuizRequest request = SubmitQuizRequest.builder()
                .submissionId(submissionId)
                .answers(List.of(
                        SubmitAnswerRequest.builder().questionId(UUID.randomUUID()).selectedAnswer("A").build()
                ))
                .build();

        SubmissionResponse response = SubmissionResponse.builder()
                .id(submissionId)
                .quizId(quizId)
                .userId(userId)
                .score(10.0)
                .percentage(100.0)
                .status(SubmissionStatus.SUBMITTED)
                .submittedAt(Instant.now())
                .build();

        when(submissionService.submitQuiz(any(), eq(userId))).thenReturn(response);

        mockMvc.perform(post("/api/v1/submissions/submit")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(submissionId.toString()))
                .andExpect(jsonPath("$.data.score").value(10.0))
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));
    }

    @Test
    @DisplayName("GET /api/v1/submissions/{id} should return submission")
    void testGetSubmission() throws Exception {
        SubmissionResponse response = SubmissionResponse.builder()
                .id(submissionId)
                .quizId(quizId)
                .userId(userId)
                .status(SubmissionStatus.SUBMITTED)
                .build();

        when(submissionService.getSubmission(submissionId, userId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/submissions/" + submissionId)
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(submissionId.toString()));
    }
}
