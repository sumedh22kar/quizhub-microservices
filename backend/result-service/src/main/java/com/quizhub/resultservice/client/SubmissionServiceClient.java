package com.quizhub.resultservice.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "submission-service")
public interface SubmissionServiceClient {
}
