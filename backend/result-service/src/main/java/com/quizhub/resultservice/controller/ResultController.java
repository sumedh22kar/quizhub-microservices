package com.quizhub.resultservice.controller;

import com.quizhub.resultservice.common.ApiResponse;
import com.quizhub.resultservice.dto.response.ResultResponse;
import com.quizhub.resultservice.service.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/results")
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ResultResponse>> getResult(@PathVariable("id") UUID id) {
        ResultResponse response = resultService.getResult(id);
        return ResponseEntity.ok(ApiResponse.<ResultResponse>builder()
                .success(true)
                .message("Result retrieved successfully.")
                .data(response)
                .build());
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<ResultResponse>>> getMyResults() {
        List<ResultResponse> results = resultService.getMyResults();
        return ResponseEntity.ok(ApiResponse.<List<ResultResponse>>builder()
                .success(true)
                .message("User results retrieved successfully.")
                .data(results)
                .build());
    }

    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<ApiResponse<List<ResultResponse>>> getQuizResults(@PathVariable("quizId") UUID quizId) {
        List<ResultResponse> results = resultService.getQuizResults(quizId);
        return ResponseEntity.ok(ApiResponse.<List<ResultResponse>>builder()
                .success(true)
                .message("Quiz results retrieved successfully.")
                .data(results)
                .build());
    }

    @GetMapping("/leaderboard/{quizId}")
    public ResponseEntity<ApiResponse<List<ResultResponse>>> getLeaderboard(@PathVariable("quizId") UUID quizId) {
        List<ResultResponse> leaderboard = resultService.getLeaderboard(quizId);
        return ResponseEntity.ok(ApiResponse.<List<ResultResponse>>builder()
                .success(true)
                .message("Leaderboard retrieved successfully.")
                .data(leaderboard)
                .build());
    }
}
