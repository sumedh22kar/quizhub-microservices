package com.quizhub.aiagent.application;

import com.quizhub.aiagent.dto.InternalQuestionResponse;
import com.quizhub.aiagent.dto.internal.InternalSubmissionAnswerResponse;
import com.quizhub.aiagent.dto.response.QuestionAnalysisResponse;
import com.quizhub.aiagent.infrastructure.llm.LLMService;
import com.quizhub.aiagent.prompt.PromptBuilder;
import com.quizhub.aiagent.service.AiResponseParser;
import com.quizhub.aiagent.service.AiRetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiCacheService {

    private final PromptBuilder promptBuilder;
    private final LLMService llmService;
    private final AiResponseParser aiResponseParser;
    private final AiRetryService aiRetryService;

    @Cacheable(value = "ai-explain", key = "#question.id")
    public String generateExplain(InternalQuestionResponse question) {
        log.info("🔥 LLM CALLED: generateExplain for question {}", question.getId());
        String prompt = promptBuilder.buildExplainPrompt(question);
        return llmService.chat(prompt);
    }

    @Cacheable(value = "ai-hint", key = "#question.id")
    public String generateHint(InternalQuestionResponse question) {
        log.info("🔥 LLM CALLED: generateHint for question {}", question.getId());
        String prompt = promptBuilder.buildHintPrompt(question);
        return llmService.chat(prompt);
    }

    @Cacheable(value = "ai-analysis", key = "#question.id")
    public QuestionAnalysisResponse generateAnalysis(InternalQuestionResponse question) {
        log.info("🔥 LLM CALLED: generateAnalysis for question {}", question.getId());
        String prompt = promptBuilder.buildAnalysisPrompt(question);
        return aiRetryService.executeWithRetry(
                prompt,
                aiResponseParser::parseAnalysis
        );
    }

    @Cacheable(value = "ai-review", key = "#submissionId")
    public String generateReview(
            UUID submissionId,
            List<InternalQuestionResponse> questions,
            List<InternalSubmissionAnswerResponse> answers
    ) {
        log.info("🔥 LLM CALLED: generateReview for submission {}", submissionId);
        String prompt = promptBuilder.buildReviewSubmissionPrompt(questions, answers);
        return llmService.chat(prompt);
    }

    @Cacheable(value = "ai-study-plan", key = "#submissionId")
    public String generateStudyPlan(
            UUID submissionId,
            List<InternalQuestionResponse> questions,
            List<InternalSubmissionAnswerResponse> answers
    ) {
        log.info("🔥 LLM CALLED: generateStudyPlan for submission {}", submissionId);
        String prompt = promptBuilder.buildStudyPlanPrompt(questions, answers);
        return llmService.chat(prompt);
    }
}
