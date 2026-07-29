package com.quizhub.questionservice.controller;

import com.quizhub.questionservice.common.ApiResponse;
import com.quizhub.questionservice.dto.request.CreateQuestionRequest;
import com.quizhub.questionservice.dto.request.UpdateQuestionRequest;
import com.quizhub.questionservice.dto.response.QuestionResponse;
import com.quizhub.questionservice.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    public ResponseEntity<ApiResponse<QuestionResponse>> createQuestion(
            @Valid @RequestBody CreateQuestionRequest request
    ) {
        QuestionResponse response = questionService.createQuestion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<QuestionResponse>builder()
                        .success(true)
                        .message("Question created successfully.")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuestionResponse>> getQuestionById(
            @PathVariable UUID id
    ) {
        QuestionResponse response = questionService.getQuestionById(id);
        return ResponseEntity.ok(
                ApiResponse.<QuestionResponse>builder()
                        .success(true)
                        .message("Question fetched successfully.")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getQuestionsByQuizId(
            @PathVariable UUID quizId
    ) {
        List<QuestionResponse> response = questionService.getQuestionsByQuizId(quizId);
        return ResponseEntity.ok(
                ApiResponse.<List<QuestionResponse>>builder()
                        .success(true)
                        .message("Questions for quiz fetched successfully.")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<QuestionResponse>> updateQuestion(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateQuestionRequest request
    ) {
        QuestionResponse response = questionService.updateQuestion(id, request);
        return ResponseEntity.ok(
                ApiResponse.<QuestionResponse>builder()
                        .success(true)
                        .message("Question updated successfully.")
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<QuestionResponse>> activateQuestion(
            @PathVariable UUID id
    ) {
        QuestionResponse response = questionService.activateQuestion(id);
        return ResponseEntity.ok(
                ApiResponse.<QuestionResponse>builder()
                        .success(true)
                        .message("Question activated successfully.")
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<QuestionResponse>> deactivateQuestion(
            @PathVariable UUID id
    ) {
        QuestionResponse response = questionService.deactivateQuestion(id);
        return ResponseEntity.ok(
                ApiResponse.<QuestionResponse>builder()
                        .success(true)
                        .message("Question deactivated successfully.")
                        .data(response)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(
            @PathVariable UUID id
    ) {
        questionService.deleteQuestion(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Question deleted successfully.")
                        .build()
        );
    }
}