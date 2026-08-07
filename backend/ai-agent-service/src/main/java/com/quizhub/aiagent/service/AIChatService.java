package com.quizhub.aiagent.service;

import com.quizhub.aiagent.dto.ChatRequest;
import com.quizhub.aiagent.dto.ChatResponse;

public interface AIChatService {

    ChatResponse chat(ChatRequest request);

}