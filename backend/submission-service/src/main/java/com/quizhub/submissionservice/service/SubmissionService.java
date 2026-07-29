package com.quizhub.submissionservice.service;

import com.quizhub.submissionservice.dto.request.StartSubmissionRequest;
import com.quizhub.submissionservice.dto.request.SubmitQuizRequest;
import com.quizhub.submissionservice.dto.response.SubmissionResponse;

import java.util.List;
import java.util.UUID;

public interface SubmissionService {

    SubmissionResponse startQuiz(StartSubmissionRequest request, UUID userId);

    SubmissionResponse submitQuiz(SubmitQuizRequest request, UUID userId);

    SubmissionResponse getSubmission(UUID submissionId, UUID userId);

    List<SubmissionResponse> getMySubmissions(UUID userId);
}