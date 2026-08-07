package com.quizhub.aiagent.application;

import com.quizhub.aiagent.dto.ChatRequest;
import com.quizhub.aiagent.dto.ChatResponse;
import com.quizhub.aiagent.infrastructure.llm.LLMService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AIChatService {

    private final LLMService llmService;

    public ChatResponse chat(ChatRequest request) {
        String answer = llmService.chat(request.getMessage());

        return ChatResponse.builder()
                .response(answer)
                .build();
    }
}
