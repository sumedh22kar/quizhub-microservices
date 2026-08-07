package com.quizhub.aiagent.controller;
import com.quizhub.aiagent.client.QuestionServiceClient;
import com.quizhub.aiagent.client.QuizServiceClient;
import com.quizhub.aiagent.client.SubmissionServiceClient;
import com.quizhub.aiagent.dto.InternalQuestionResponse;
import com.quizhub.aiagent.dto.internal.InternalQuizResponse;
import com.quizhub.aiagent.dto.internal.InternalSubmissionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/ai/test")
@RequiredArgsConstructor
public class SubmitController {
    private final SubmissionServiceClient submissionClient;
    private final QuizServiceClient quizClient;
    private final QuestionServiceClient questionClient;

    @GetMapping("/{submissionId}")
    public Map<String, Object> test(
            @PathVariable UUID submissionId
    ) {

        InternalSubmissionResponse submission =
                submissionClient.getSubmission(submissionId);

        InternalQuizResponse quiz =
                quizClient.getQuiz(submission.getQuizId());

        InternalQuestionResponse firstQuestion = null;

        if (submission.getAnswers() != null &&
                !submission.getAnswers().isEmpty()) {

            UUID questionId =
                    submission.getAnswers().get(0).getQuestionId();

            firstQuestion =
                    questionClient.getQuestion(questionId);
        }

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("submission", submission);
        response.put("quiz", quiz);
        response.put("firstQuestion", firstQuestion);

        return response;
    }
}
