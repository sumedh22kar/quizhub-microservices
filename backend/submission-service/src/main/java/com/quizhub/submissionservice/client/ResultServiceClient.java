package com.quizhub.submissionservice.client;

import com.quizhub.submissionservice.dto.internal.GenerateResultRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "result-service")
public interface ResultServiceClient {

    @PostMapping("/api/v1/internal/results")
    Object generateResult(@RequestBody GenerateResultRequest request);
}
