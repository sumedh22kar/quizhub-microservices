package com.quizhub.aiagent.application.review;

import com.quizhub.aiagent.client.QuestionServiceClient;
import com.quizhub.aiagent.client.SubmissionServiceClient;
import com.quizhub.aiagent.dto.internal.BatchQuestionRequest;
import com.quizhub.aiagent.dto.InternalQuestionResponse;
import com.quizhub.aiagent.dto.internal.InternalSubmissionResponse;
import com.quizhub.aiagent.infrastructure.llm.LLMService;
import com.quizhub.aiagent.prompt.PromptBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewSubmissionService {

    private final SubmissionServiceClient submissionServiceClient;
    private final QuestionServiceClient questionServiceClient;
    private final PromptBuilder promptBuilder;
    private final LLMService llmService;

    public String reviewSubmission(UUID submissionId) {

        // Step 1
        InternalSubmissionResponse submission =
                submissionServiceClient.getSubmission(submissionId);

        // Step 2
        List<UUID> questionIds =
                submission.getAnswers()
                        .stream()
                        .map(answer -> answer.getQuestionId())
                        .toList();

        // Step 3
        List<InternalQuestionResponse> questions =
                questionServiceClient.getQuestions(
                        BatchQuestionRequest.builder()
                                .questionIds(questionIds)
                                .build()
                );

        // Step 4
        String prompt =
                promptBuilder.buildReviewSubmissionPrompt(
                        questions,
                        submission.getAnswers()
                );

        // Step 5
        return llmService.chat(prompt);
    }
}