package com.quizhub.aiagent.controller;

import com.quizhub.aiagent.application.AnalyzeQuestionService;
import com.quizhub.aiagent.dto.response.QuestionAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AnalyzeQuestionController {

    private final AnalyzeQuestionService service;

    @PostMapping("/analyze-question/{id}")
    public ResponseEntity<QuestionAnalysisResponse> analyze(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                service.analyze(id)
        );

    }
}