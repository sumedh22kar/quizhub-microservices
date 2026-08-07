package com.quizhub.aiagent.client;

import com.quizhub.aiagent.dto.internal.InternalSubmissionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "submission-service")
public interface SubmissionServiceClient {

    @GetMapping("/api/v1/internal/submissions/{id}")
    InternalSubmissionResponse getSubmission(
            @PathVariable UUID id
    );

}