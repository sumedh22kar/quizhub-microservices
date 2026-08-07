package com.quizhub.aiagent.client;

import com.quizhub.aiagent.dto.InternalQuestionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "question-service")
public interface QuestionServiceClient {

    @GetMapping("/api/v1/internal/questions/{id}")
    InternalQuestionResponse getQuestion(
            @PathVariable UUID id
    );

}