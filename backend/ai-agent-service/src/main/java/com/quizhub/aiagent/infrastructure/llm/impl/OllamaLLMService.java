package com.quizhub.aiagent.infrastructure.llm.impl;

import com.quizhub.aiagent.infrastructure.llm.LLMService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class OllamaLLMService implements LLMService {

    private final ChatClient chatClient;

    public OllamaLLMService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public String chat(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}