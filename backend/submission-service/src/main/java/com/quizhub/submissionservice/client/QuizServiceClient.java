package com.quizhub.submissionservice.client;

import com.quizhub.submissionservice.dto.internal.InternalQuizResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "quiz-service", path = "/api/v1/internal/quizzes")
public interface QuizServiceClient {

    @GetMapping("/{quizId}")
    InternalQuizResponse getQuiz(@PathVariable("quizId") UUID quizId);
}