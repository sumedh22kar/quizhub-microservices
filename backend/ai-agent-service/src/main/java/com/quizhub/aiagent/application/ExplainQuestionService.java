package com.quizhub.aiagent.application;

import com.quizhub.aiagent.client.QuestionServiceClient;
import com.quizhub.aiagent.dto.InternalQuestionResponse;
import com.quizhub.aiagent.dto.response.AIResponse;
import com.quizhub.aiagent.infrastructure.llm.LLMService;
import com.quizhub.aiagent.prompt.PromptBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExplainQuestionService {

    private final QuestionServiceClient questionClient;
    private final PromptBuilder promptBuilder;
    private final LLMService llmService;

    public AIResponse explain(UUID questionId) {

        long start = System.currentTimeMillis();

        log.info("Fetching question {}", questionId);
        InternalQuestionResponse question =
                questionClient.getQuestion(questionId);

        log.info("Building prompt");
        String prompt =
                promptBuilder.buildExplainPrompt(question);

        log.info("Calling Ollama");
        String answer =
                llmService.chat(prompt);

        long end = System.currentTimeMillis();

        log.info("Explanation generated");
        return AIResponse.builder()
                .answer(answer)
                .model("qwen3:8b")
                .responseTime(end - start)
                .build();
    }
}
