package com.quizhub.resultservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizhub.resultservice.entity.Result;
import com.quizhub.resultservice.repository.ResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ResultIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResultRepository resultRepository;

    private UUID userId;
    private UUID quizId;
    private UUID submissionId;

    @BeforeEach
    void setUp() {
        resultRepository.deleteAll();
        userId = UUID.randomUUID();
        quizId = UUID.randomUUID();
        submissionId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Generate Result via internal API & Retrieve via GET /api/v1/results/{id}")
    void testGenerateAndGetResult() throws Exception {
        com.quizhub.resultservice.dto.internal.GenerateResultRequest request = com.quizhub.resultservice.dto.internal.GenerateResultRequest.builder()
                .submissionId(submissionId)
                .quizId(quizId)
                .userId(userId)
                .score(70.0)
                .percentage(70.0)
                .totalQuestions(10)
                .correctAnswers(7)
                .wrongAnswers(3)
                .build();

        // Generate result via internal endpoint
        mockMvc.perform(post("/api/v1/internal/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.score").value(70.0))
                .andExpect(jsonPath("$.data.totalQuestions").value(10))
                .andExpect(jsonPath("$.data.correctAnswers").value(7))
                .andExpect(jsonPath("$.data.wrongAnswers").value(3))
                .andExpect(jsonPath("$.data.passed").value(true));

        // DB Verification
        Result dbResult = resultRepository.findBySubmissionId(submissionId).orElseThrow();
        assertThat(dbResult.getQuizId()).isEqualTo(quizId);

        // GET by ID
        mockMvc.perform(get("/api/v1/results/" + dbResult.getId())
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(dbResult.getId().toString()));
    }

    @Test
    @DisplayName("GET /api/v1/results/me — Should return user results")
    void testGetMyResults() throws Exception {
        Result result = resultRepository.save(Result.builder()
                .submissionId(submissionId)
                .quizId(quizId)
                .userId(userId)
                .score(90.0)
                .percentage(90.0)
                .totalQuestions(5)
                .correctAnswers(4)
                .wrongAnswers(1)
                .passed(true)
                .completedAt(LocalDateTime.now())
                .build());

        mockMvc.perform(get("/api/v1/results/me")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(result.getId().toString()));
    }

    @Test
    @DisplayName("GET /api/v1/results/leaderboard/{quizId} — Should return ranked leaderboard by score")
    void testGetLeaderboard() throws Exception {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        resultRepository.save(Result.builder()
                .submissionId(UUID.randomUUID())
                .quizId(quizId)
                .userId(user1)
                .score(60.0)
                .percentage(60.0)
                .completedAt(LocalDateTime.now())
                .build());

        resultRepository.save(Result.builder()
                .submissionId(UUID.randomUUID())
                .quizId(quizId)
                .userId(user2)
                .score(95.0)
                .percentage(95.0)
                .completedAt(LocalDateTime.now().minusMinutes(5))
                .build());

        mockMvc.perform(get("/api/v1/results/leaderboard/" + quizId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].userId").value(user2.toString()))
                .andExpect(jsonPath("$.data[0].score").value(95.0))
                .andExpect(jsonPath("$.data[1].userId").value(user1.toString()))
                .andExpect(jsonPath("$.data[1].score").value(60.0));
    }
}
