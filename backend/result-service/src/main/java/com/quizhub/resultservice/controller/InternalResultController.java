package com.quizhub.resultservice.controller;

import com.quizhub.resultservice.common.ApiResponse;
import com.quizhub.resultservice.dto.internal.GenerateResultRequest;
import com.quizhub.resultservice.dto.response.ResultResponse;
import com.quizhub.resultservice.service.ResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/internal/results")
@RequiredArgsConstructor
public class InternalResultController {

    private final ResultService resultService;

    @PostMapping
    public ResponseEntity<ApiResponse<ResultResponse>> generateResult(
            @Valid @RequestBody GenerateResultRequest request) {

        ResultResponse response = resultService.generateResult(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ResultResponse>builder()
                        .success(true)
                        .message("Result generated successfully.")
                        .data(response)
                        .build());
    }
}
