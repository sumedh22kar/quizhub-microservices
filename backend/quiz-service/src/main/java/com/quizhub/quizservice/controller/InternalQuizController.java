package com.quizhub.quizservice.controller;

import com.quizhub.quizservice.dto.internal.InternalQuizResponse;
import com.quizhub.quizservice.dto.response.QuizOwnerResponse;
import com.quizhub.quizservice.dto.response.QuizResponse;
import com.quizhub.quizservice.exception.AccessDeniedException;
import com.quizhub.quizservice.exception.ResourceNotFoundException;
import com.quizhub.quizservice.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/quizzes")
@RequiredArgsConstructor
public class InternalQuizController {

    private final QuizService quizService;

    @GetMapping("/{quizId}/exists")
    public ResponseEntity<Void> quizExists(
            @PathVariable UUID quizId) {

        if (!quizService.quizExists(quizId)) {
            throw new ResourceNotFoundException("Quiz not found.");
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{quizId}/owner")
    public QuizOwnerResponse getOwner(
            @PathVariable UUID quizId) {

        return quizService.getQuizOwner(quizId);
    }

    @GetMapping("/{quizId}/verify-owner")
    public ResponseEntity<Void> verifyQuizOwner(
            @PathVariable UUID quizId,
            @RequestParam UUID userId) {

        QuizResponse quiz = quizService.getQuizById(quizId);
        if (!quiz.getOwnerId().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to modify questions for this quiz.");
        }

        return ResponseEntity.ok().build();
    }
    @GetMapping("/{quizId}")
    public ResponseEntity<InternalQuizResponse> getQuiz(
            @PathVariable UUID quizId) {

        return ResponseEntity.ok(
                quizService.getInternalQuiz(quizId)
        );
    }
}