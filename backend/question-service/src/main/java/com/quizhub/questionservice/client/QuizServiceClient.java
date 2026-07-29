package com.quizhub.questionservice.client;

import com.quizhub.questionservice.dto.response.QuizOwnerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "quiz-service", url = "${QUIZ_SERVICE_URL:http://localhost:9002}")
public interface QuizServiceClient {

    @GetMapping("/api/v1/internal/quizzes/{quizId}/exists")
    void verifyQuizExists(@PathVariable("quizId") UUID quizId);

    @GetMapping("/api/v1/internal/quizzes/{quizId}/owner")
    QuizOwnerResponse getQuizOwner(@PathVariable("quizId") UUID quizId);

    @GetMapping("/api/v1/internal/quizzes/{quizId}/verify-owner")
    void verifyQuizOwner(@PathVariable("quizId") UUID quizId, @RequestParam("userId") UUID userId);
}