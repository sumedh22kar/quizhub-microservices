package com.quizhub.questionservice.controller;

import com.quizhub.questionservice.config.UserContextInterceptor;
import com.quizhub.questionservice.dto.internal.InternalQuestionResponse;
import com.quizhub.questionservice.service.QuestionService;
import com.quizhub.questionservice.security.UserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InternalQuestionController.class)
class InternalQuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuestionService questionService;

    @MockitoBean
    private UserContext userContext;

    @MockitoBean
    private UserContextInterceptor userContextInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        when(userContextInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    @DisplayName("GET /api/v1/internal/questions/quiz/{quizId} should return internal questions list")
    void testGetInternalQuestions() throws Exception {
        UUID quizId = UUID.randomUUID();
        UUID q1Id = UUID.randomUUID();
        UUID q2Id = UUID.randomUUID();

        InternalQuestionResponse q1 = InternalQuestionResponse.builder()
                .id(q1Id)
                .quizId(quizId)
                .correctAnswer("A")
                .marks(5)
                .build();

        InternalQuestionResponse q2 = InternalQuestionResponse.builder()
                .id(q2Id)
                .quizId(quizId)
                .correctAnswer("C")
                .marks(10)
                .build();

        when(questionService.getInternalQuestions(quizId)).thenReturn(List.of(q1, q2));

        mockMvc.perform(get("/api/v1/internal/questions/quiz/" + quizId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(q1Id.toString()))
                .andExpect(jsonPath("$[0].quizId").value(quizId.toString()))
                .andExpect(jsonPath("$[0].correctAnswer").value("A"))
                .andExpect(jsonPath("$[0].marks").value(5))
                .andExpect(jsonPath("$[1].id").value(q2Id.toString()))
                .andExpect(jsonPath("$[1].quizId").value(quizId.toString()))
                .andExpect(jsonPath("$[1].correctAnswer").value("C"))
                .andExpect(jsonPath("$[1].marks").value(10));
    }
}
