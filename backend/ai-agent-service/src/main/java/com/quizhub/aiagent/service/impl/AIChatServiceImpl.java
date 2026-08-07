package com.quizhub.aiagent.service.impl;

import com.quizhub.aiagent.dto.ChatRequest;
import com.quizhub.aiagent.dto.ChatResponse;
import com.quizhub.aiagent.service.AIChatService;
import com.quizhub.aiagent.service.LLMService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AIChatServiceImpl implements AIChatService {

    private final LLMService llmService;

    @Override
    public ChatResponse chat(ChatRequest request) {

        String answer = llmService.chat(request.getMessage());

        return ChatResponse.builder()
                .response(answer)
                .build();

    }
}