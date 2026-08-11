package com.quizhub.aiagent.application;

import com.quizhub.aiagent.client.QuestionServiceClient;
import com.quizhub.aiagent.dto.InternalQuestionResponse;
import com.quizhub.aiagent.dto.response.QuestionAnalysisResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyzeQuestionService {

    private final QuestionServiceClient questionClient;
    private final AiCacheService aiCacheService;

    @Cacheable(value = "ai-analysis", key = "#questionId")
    public QuestionAnalysisResponse analyze(UUID questionId) {

        log.info("Fetching question {}", questionId);

        InternalQuestionResponse question =
                questionClient.getQuestion(questionId);

        return aiCacheService.generateAnalysis(question);
    }
}