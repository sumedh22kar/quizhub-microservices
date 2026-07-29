package com.quizhub.submissionservice.client;

import com.quizhub.submissionservice.dto.internal.InternalQuestionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "question-service", path = "/api/v1/internal/questions")
public interface QuestionServiceClient {

    @GetMapping("/quiz/{quizId}")
    List<InternalQuestionResponse> getQuestions(@PathVariable("quizId") UUID quizId);
}