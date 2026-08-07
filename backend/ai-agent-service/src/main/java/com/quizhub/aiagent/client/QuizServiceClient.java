package com.quizhub.aiagent.client;

import com.quizhub.aiagent.dto.internal.InternalQuizResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "quiz-service")
public interface QuizServiceClient {

    @GetMapping("/api/v1/internal/quizzes/{quizId}")
    InternalQuizResponse getQuiz(
            @PathVariable UUID quizId
    );

}