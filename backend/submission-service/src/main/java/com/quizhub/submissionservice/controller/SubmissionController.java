package com.quizhub.submissionservice.controller;

import com.quizhub.submissionservice.common.ApiResponse;
import com.quizhub.submissionservice.dto.request.StartSubmissionRequest;
import com.quizhub.submissionservice.dto.request.SubmitQuizRequest;
import com.quizhub.submissionservice.dto.response.SubmissionResponse;
import com.quizhub.submissionservice.security.UserContext;
import com.quizhub.submissionservice.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;
    private final UserContext userContext;

    @PostMapping("/start")
    public ResponseEntity<ApiResponse<SubmissionResponse>> startQuiz(
            @Valid @RequestBody StartSubmissionRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {

        UUID userId = resolveUserId(headerUserId);
        SubmissionResponse response = submissionService.startQuiz(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<SubmissionResponse>builder()
                        .success(true)
                        .message("Quiz submission started successfully.")
                        .data(response)
                        .build());
    }

    @PostMapping("/{submissionId}/submit")
    public ResponseEntity<ApiResponse<SubmissionResponse>> submitQuizWithPathVariable(
            @PathVariable("submissionId") UUID submissionId,
            @Valid @RequestBody SubmitQuizRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {

        request.setSubmissionId(submissionId);
        UUID userId = resolveUserId(headerUserId);
        SubmissionResponse response = submissionService.submitQuiz(request, userId);
        return ResponseEntity.ok(ApiResponse.<SubmissionResponse>builder()
                .success(true)
                .message("Quiz submitted successfully.")
                .data(response)
                .build());
    }

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<SubmissionResponse>> submitQuiz(
            @Valid @RequestBody SubmitQuizRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {

        UUID userId = resolveUserId(headerUserId);
        SubmissionResponse response = submissionService.submitQuiz(request, userId);
        return ResponseEntity.ok(ApiResponse.<SubmissionResponse>builder()
                .success(true)
                .message("Quiz submitted successfully.")
                .data(response)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubmissionResponse>> getSubmission(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {

        UUID userId = resolveUserId(headerUserId);
        SubmissionResponse response = submissionService.getSubmission(id, userId);
        return ResponseEntity.ok(ApiResponse.<SubmissionResponse>builder()
                .success(true)
                .message("Submission retrieved successfully.")
                .data(response)
                .build());
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<SubmissionResponse>>> getMySubmissions(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {

        UUID userId = resolveUserId(headerUserId);
        List<SubmissionResponse> response = submissionService.getMySubmissions(userId);
        return ResponseEntity.ok(ApiResponse.<List<SubmissionResponse>>builder()
                .success(true)
                .message("Submissions retrieved successfully.")
                .data(response)
                .build());
    }

    private UUID resolveUserId(String headerUserId) {
        if (headerUserId != null && !headerUserId.isBlank()) {
            try {
                return UUID.fromString(headerUserId);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return userContext.getUserId();
    }
}
