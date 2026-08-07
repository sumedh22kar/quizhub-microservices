package com.quizhub.aiagent.service.impl;

import com.quizhub.aiagent.service.LLMService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeminiLLMService implements LLMService {

    private final ChatClient.Builder chatClientBuilder;

    @Override
    public String chat(String message) {

        return chatClientBuilder
                .build()
                .prompt()
                .user(message)
                .call()
                .content();

    }
}