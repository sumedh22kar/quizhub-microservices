package com.quizhub.quizservice.controller;

import com.quizhub.quizservice.dto.request.CreateQuizRequest;
import com.quizhub.quizservice.dto.request.UpdateQuizRequest;
import com.quizhub.quizservice.dto.response.QuizResponse;
import com.quizhub.quizservice.service.QuizService;
import com.quizhub.quizservice.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping
    public ResponseEntity<ApiResponse<QuizResponse>> createQuiz(
            @Valid @RequestBody CreateQuizRequest request
    ) {

        QuizResponse response = quizService.createQuiz(request);

        return ResponseEntity.ok(
                ApiResponse.<QuizResponse>builder()
                        .success(true)
                        .message("Quiz created successfully.")
                        .data(response)
                        .build()
        );
    }
    @GetMapping("/{quizId}")
    public ResponseEntity<ApiResponse<QuizResponse>> getQuizById(
            @PathVariable UUID quizId
    ) {

        QuizResponse response = quizService.getQuizById(quizId);

        return ResponseEntity.ok(
                ApiResponse.<QuizResponse>builder()
                        .success(true)
                        .message("Quiz fetched successfully.")
                        .data(response)
                        .build()
        );
    }
    @PutMapping("/{quizId}")
    public ResponseEntity<ApiResponse<QuizResponse>> updateQuiz(
            @PathVariable UUID quizId,
            @Valid @RequestBody UpdateQuizRequest request
    ) {

        QuizResponse response =
                quizService.updateQuiz(quizId, request);

        return ResponseEntity.ok(
                ApiResponse.<QuizResponse>builder()
                        .success(true)
                        .message("Quiz updated successfully.")
                        .data(response)
                        .build()
        );
    }
    @DeleteMapping("/{quizId}")
    public ResponseEntity<ApiResponse<Void>> deleteQuiz(
            @PathVariable UUID quizId
    ) {

        quizService.deleteQuiz(quizId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Quiz deleted successfully.")
                        .build()
        );
    }
    @PatchMapping("/{quizId}/publish")
    public ResponseEntity<ApiResponse<QuizResponse>> publishQuiz(
            @PathVariable UUID quizId
    ) {

        QuizResponse response =
                quizService.publishQuiz(quizId);

        return ResponseEntity.ok(
                ApiResponse.<QuizResponse>builder()
                        .success(true)
                        .message("Quiz published successfully.")
                        .data(response)
                        .build()
        );
    }
    @PatchMapping("/{quizId}/archive")
    public ResponseEntity<ApiResponse<QuizResponse>> archiveQuiz(
            @PathVariable UUID quizId
    ) {

        QuizResponse response =
                quizService.archiveQuiz(quizId);

        return ResponseEntity.ok(
                ApiResponse.<QuizResponse>builder()
                        .success(true)
                        .message("Quiz archived successfully.")
                        .data(response)
                        .build()
        );
    }

}
