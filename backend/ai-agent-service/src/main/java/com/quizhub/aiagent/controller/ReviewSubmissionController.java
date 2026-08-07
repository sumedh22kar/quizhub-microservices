package com.quizhub.aiagent.controller;

import com.quizhub.aiagent.application.review.ReviewSubmissionService;
import com.quizhub.aiagent.dto.response.ReviewSubmissionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class ReviewSubmissionController {

    private final ReviewSubmissionService reviewSubmissionService;

    @PostMapping("/review-submission/{submissionId}")
    public ResponseEntity<ReviewSubmissionResponse> reviewSubmission(
            @PathVariable UUID submissionId
    ) {

        long start = System.currentTimeMillis();

        String answer =
                reviewSubmissionService.reviewSubmission(submissionId);

        long end = System.currentTimeMillis();

        return ResponseEntity.ok(
                ReviewSubmissionResponse.builder()
                        .answer(answer)
                        .model("qwen3:8b")
                        .responseTime(end - start)
                        .build()
        );
    }
}