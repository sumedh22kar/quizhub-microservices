package com.quizhub.aiagent.controller;

import com.quizhub.aiagent.application.ExplainQuestionService;
import com.quizhub.aiagent.dto.response.AIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class ExplainQuestionController {

    private final ExplainQuestionService explainQuestionService;

    @PostMapping("/explain-question/{id}")
    public ResponseEntity<AIResponse> explain(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                explainQuestionService.explain(id)
        );
    }
}