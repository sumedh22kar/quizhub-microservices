package com.quizhub.aiagent.application;

import com.quizhub.aiagent.client.QuestionServiceClient;
import com.quizhub.aiagent.dto.InternalQuestionResponse;
import com.quizhub.aiagent.dto.response.AIResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExplainQuestionService {

    private final QuestionServiceClient questionClient;
    private final AiCacheService aiCacheService;

    @Value("${spring.ai.ollama.chat.options.model:qwen3:8b}")
    private String modelName;

    @Cacheable(value = "ai-explain", key = "#questionId")
    public AIResponse explain(UUID questionId) {

        long start = System.currentTimeMillis();

        log.info("Fetching question {}", questionId);
        InternalQuestionResponse question =
                questionClient.getQuestion(questionId);

        String answer = aiCacheService.generateExplain(question);

        long end = System.currentTimeMillis();

        log.info("Explanation retrieved in {} ms", end - start);
        return AIResponse.builder()
                .answer(answer)
                .model(modelName)
                .responseTime(end - start)
                .build();
    }
}

