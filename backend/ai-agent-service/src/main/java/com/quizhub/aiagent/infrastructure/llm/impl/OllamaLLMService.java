package com.quizhub.aiagent.infrastructure.llm.impl;

import com.quizhub.aiagent.infrastructure.llm.LLMService;
import com.quizhub.aiagent.metrics.AiMetrics;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OllamaLLMService implements LLMService {

    private final ChatClient chatClient;
    private final AiMetrics aiMetrics;

    public OllamaLLMService(
            ChatClient.Builder builder,
            AiMetrics aiMetrics
    ) {
        this.chatClient = builder.build();
        this.aiMetrics = aiMetrics;
    }

    @Override
    public String chat(String message) {

        aiMetrics.llmRequests().increment();

        Timer.Sample sample =
                Timer.start();

        try {

            String response = chatClient.prompt()
                    .user(message)
                    .call()
                    .content();

            sample.stop(aiMetrics.llmDuration());

            return response;

        } catch (Exception ex) {

            sample.stop(aiMetrics.llmDuration());

            aiMetrics.llmFailures().increment();

            log.error(
                    "LLM request failed",
                    ex
            );

            throw ex;
        }
    }
}