package com.quizhub.questionservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizhub.questionservice.config.UserContextInterceptor;
import com.quizhub.questionservice.dto.request.CreateQuestionRequest;
import com.quizhub.questionservice.dto.request.UpdateQuestionRequest;
import com.quizhub.questionservice.dto.response.QuestionResponse;
import com.quizhub.questionservice.entity.enums.QuestionType;
import com.quizhub.questionservice.exception.ResourceNotFoundException;
import com.quizhub.questionservice.service.QuestionService;
import com.quizhub.questionservice.security.UserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QuestionController.class)
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private QuestionService questionService;

    @MockitoBean
    private UserContext userContext;

    @MockitoBean
    private UserContextInterceptor userContextInterceptor;

    private UUID questionId;
    private UUID quizId;
    private CreateQuestionRequest createRequest;
    private UpdateQuestionRequest updateRequest;
    private QuestionResponse response;

    @BeforeEach
    void setUp() throws Exception {
        when(userContextInterceptor.preHandle(any(), any(), any())).thenReturn(true);

        questionId = UUID.randomUUID();
        quizId = UUID.randomUUID();

        createRequest = CreateQuestionRequest.builder()
                .quizId(quizId)
                .questionText("What is Java?")
                .questionType(QuestionType.MCQ)
                .optionA("Language")
                .optionB("Database")
                .correctAnswer("Language")
                .marks(5)
                .build();

        updateRequest = UpdateQuestionRequest.builder()
                .quizId(quizId)
                .questionText("What is Java 21?")
                .questionType(QuestionType.MCQ)
                .optionA("Language")
                .optionB("Database")
                .correctAnswer("Language")
                .marks(10)
                .build();

        response = QuestionResponse.builder()
                .id(questionId)
                .quizId(quizId)
                .questionText("What is Java?")
                .questionType(QuestionType.MCQ)
                .optionA("Language")
                .optionB("Database")
                .correctAnswer("Language")
                .marks(5)
                .active(true)
                .build();
    }

    @Test
    void createQuestion_Success() throws Exception {
        when(questionService.createQuestion(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(questionId.toString()));
    }

    @Test
    void getQuestionById_Success() throws Exception {
        when(questionService.getQuestionById(questionId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/questions/{id}", questionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.questionText").value("What is Java?"));
    }

    @Test
    void getQuestionById_NotFound_Returns404() throws Exception {
        when(questionService.getQuestionById(questionId))
                .thenThrow(new ResourceNotFoundException("Question not found with id: " + questionId));

        mockMvc.perform(get("/api/v1/questions/{id}", questionId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getQuestionsByQuizId_Success() throws Exception {
        when(questionService.getQuestionsByQuizId(quizId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/questions/quiz/{quizId}", quizId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(questionId.toString()));
    }

    @Test
    void updateQuestion_Success() throws Exception {
        when(questionService.updateQuestion(eq(questionId), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/questions/{id}", questionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void activateQuestion_Success() throws Exception {
        when(questionService.activateQuestion(questionId)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/questions/{id}/activate", questionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deactivateQuestion_Success() throws Exception {
        when(questionService.deactivateQuestion(questionId)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/questions/{id}/deactivate", questionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteQuestion_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/questions/{id}", questionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
