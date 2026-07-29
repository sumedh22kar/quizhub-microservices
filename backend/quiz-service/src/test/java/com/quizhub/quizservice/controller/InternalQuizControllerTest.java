package com.quizhub.quizservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizhub.quizservice.entity.Quiz;
import com.quizhub.quizservice.entity.enums.Difficulty;
import com.quizhub.quizservice.entity.enums.QuizStatus;
import com.quizhub.quizservice.repository.QuizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class InternalQuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        quizRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/v1/internal/quizzes/{quizId} should return internal quiz details")
    void testGetInternalQuiz() throws Exception {
        UUID ownerId = UUID.randomUUID();
        Quiz quiz = Quiz.builder()
                .title("General Knowledge")
                .description("Test Quiz")
                .difficulty(Difficulty.MEDIUM)
                .durationMinutes(30)
                .totalMarks(100)
                .status(QuizStatus.PUBLISHED)
                .ownerId(ownerId)
                .build();

        Quiz savedQuiz = quizRepository.save(quiz);

        mockMvc.perform(get("/api/v1/internal/quizzes/" + savedQuiz.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedQuiz.getId().toString()))
                .andExpect(jsonPath("$.ownerId").value(ownerId.toString()))
                .andExpect(jsonPath("$.totalMarks").value(100))
                .andExpect(jsonPath("$.durationMinutes").value(30))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    @DisplayName("GET /api/v1/internal/quizzes/{quizId} should return 404 if quiz not found")
    void testGetInternalQuiz_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/internal/quizzes/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
