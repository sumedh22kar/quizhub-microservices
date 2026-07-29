package com.quizhub.quizservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizhub.quizservice.dto.request.CreateQuizRequest;
import com.quizhub.quizservice.dto.request.UpdateQuizRequest;
import com.quizhub.quizservice.entity.enums.Difficulty;
import com.quizhub.quizservice.repository.QuizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class QuizControllerTest {

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
    void testFullQuizLifecycle_PostmanFlow() throws Exception {
        String userId = UUID.randomUUID().toString();

        // Step 1: Create Quiz (POST /api/v1/quizzes)
        CreateQuizRequest createRequest = CreateQuizRequest.builder()
                .title("Java Basics")
                .description("Java Fundamentals Quiz")
                .difficulty(Difficulty.EASY)
                .durationMinutes(30)
                .totalMarks(100)
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/quizzes")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Quiz created successfully."))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.title").value("Java Basics"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn();

        JsonNode createResponseBody = objectMapper.readTree(createResult.getResponse().getContentAsString());
        String quizIdStr = createResponseBody.get("data").get("id").asText();
        UUID quizId = UUID.fromString(quizIdStr);

        // Step 2: Get Quiz (GET /api/v1/quizzes/{id})
        mockMvc.perform(get("/api/v1/quizzes/" + quizId)
                        .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(quizIdStr))
                .andExpect(jsonPath("$.data.title").value("Java Basics"));

        // Step 3: Update Quiz (PUT /api/v1/quizzes/{id})
        UpdateQuizRequest updateRequest = UpdateQuizRequest.builder()
                .title("Java Basics Advanced")
                .description("Updated Java Fundamentals Quiz")
                .difficulty(Difficulty.MEDIUM)
                .durationMinutes(45)
                .totalMarks(150)
                .build();

        mockMvc.perform(put("/api/v1/quizzes/" + quizId)
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Java Basics Advanced"))
                .andExpect(jsonPath("$.data.description").value("Updated Java Fundamentals Quiz"))
                .andExpect(jsonPath("$.data.difficulty").value("MEDIUM"));

        // Step 4: Publish Quiz (PATCH /api/v1/quizzes/{id}/publish)
        mockMvc.perform(patch("/api/v1/quizzes/" + quizId + "/publish")
                        .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));

        // Step 5: Archive Quiz (PATCH /api/v1/quizzes/{id}/archive)
        mockMvc.perform(patch("/api/v1/quizzes/" + quizId + "/archive")
                        .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ARCHIVED"));

        // Step 6: Delete Quiz (DELETE /api/v1/quizzes/{id})
        mockMvc.perform(delete("/api/v1/quizzes/" + quizId)
                        .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Quiz deleted successfully."));

        // Step 7: Verify Deletion (GET /api/v1/quizzes/{id}) -> 404 Not Found
        mockMvc.perform(get("/api/v1/quizzes/" + quizId)
                        .header("X-User-Id", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Quiz not found."));
    }
}
