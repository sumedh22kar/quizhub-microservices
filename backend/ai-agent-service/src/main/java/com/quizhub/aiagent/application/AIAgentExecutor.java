package com.quizhub.aiagent.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizhub.aiagent.infrastructure.llm.LLMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIAgentExecutor {

    private final LLMService llmService;
    private final ObjectMapper objectMapper;

    public String executePrompt(String prompt) {

        log.info("Sending prompt to Ollama...");

        long start = System.currentTimeMillis();

        String response = llmService.chat(prompt);

        long end = System.currentTimeMillis();

        log.info("AI completed in {} ms", end - start);

        return response;
    }

    public <T> T executeJsonPrompt(
            String prompt,
            Class<T> responseType
    ) {

        try {

            String response = executePrompt(prompt);

            return objectMapper.readValue(
                    response,
                    responseType
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to parse AI response",
                    e
            );

        }

    }

}