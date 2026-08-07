package com.quizhub.aiagent.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizhub.aiagent.client.QuestionServiceClient;
import com.quizhub.aiagent.dto.InternalQuestionResponse;
import com.quizhub.aiagent.dto.response.QuestionAnalysisResponse;
import com.quizhub.aiagent.infrastructure.llm.LLMService;
import com.quizhub.aiagent.prompt.PromptBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyzeQuestionService {

    private final QuestionServiceClient questionClient;
    private final PromptBuilder promptBuilder;
    private final LLMService llmService;
    private final ObjectMapper objectMapper;

    public QuestionAnalysisResponse analyze(UUID questionId) {

        log.info("Fetching question {}", questionId);

        InternalQuestionResponse question =
                questionClient.getQuestion(questionId);

        String prompt =
                promptBuilder.buildAnalysisPrompt(question);

        log.info("Calling Ollama");

        String response =
                llmService.chat(prompt);

        log.info("AI Response: {}", response);

        try {

            return objectMapper.readValue(
                    response,
                    QuestionAnalysisResponse.class
            );

        } catch (Exception e) {

            throw new RuntimeException("Failed to parse AI response", e);

        }

    }

}