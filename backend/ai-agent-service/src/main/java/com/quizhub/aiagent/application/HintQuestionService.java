package com.quizhub.aiagent.application;

import com.quizhub.aiagent.client.QuestionServiceClient;
import com.quizhub.aiagent.dto.InternalQuestionResponse;
import com.quizhub.aiagent.dto.response.AIResponse;
import com.quizhub.aiagent.infrastructure.llm.LLMService;
import com.quizhub.aiagent.prompt.PromptBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HintQuestionService {

    private final QuestionServiceClient questionClient;
    private final PromptBuilder promptBuilder;
    private final LLMService llmService;

    public AIResponse generateHint(UUID questionId) {

        long start = System.currentTimeMillis();

        InternalQuestionResponse question =
                questionClient.getQuestion(questionId);

        String prompt =
                promptBuilder.buildHintPrompt(question);

        String answer =
                llmService.chat(prompt);

        long end = System.currentTimeMillis();

        return AIResponse.builder()
                .answer(answer)
                .model("qwen3:8b")
                .responseTime(end - start)
                .build();
    }
}