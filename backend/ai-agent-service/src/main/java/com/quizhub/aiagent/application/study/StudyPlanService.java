package com.quizhub.aiagent.application.study;

import com.quizhub.aiagent.application.AiCacheService;
import com.quizhub.aiagent.client.QuestionServiceClient;
import com.quizhub.aiagent.client.SubmissionServiceClient;
import com.quizhub.aiagent.dto.InternalQuestionResponse;
import com.quizhub.aiagent.dto.internal.BatchQuestionRequest;
import com.quizhub.aiagent.dto.internal.InternalSubmissionAnswerResponse;
import com.quizhub.aiagent.dto.internal.InternalSubmissionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudyPlanService {

    private final SubmissionServiceClient submissionServiceClient;
    private final QuestionServiceClient questionServiceClient;
    private final AiCacheService aiCacheService;

    @Cacheable(value = "ai-study-plan", key = "#submissionId")
    public String generateStudyPlan(UUID submissionId) {

        InternalSubmissionResponse submission =
                submissionServiceClient.getSubmission(submissionId);

        List<UUID> questionIds =
                submission.getAnswers()
                        .stream()
                        .map(InternalSubmissionAnswerResponse::getQuestionId)
                        .toList();

        List<InternalQuestionResponse> questions =
                questionServiceClient.getQuestions(
                        BatchQuestionRequest.builder()
                                .questionIds(questionIds)
                                .build()
                );

        return aiCacheService.generateStudyPlan(
                submissionId,
                questions,
                submission.getAnswers()
        );
    }
}