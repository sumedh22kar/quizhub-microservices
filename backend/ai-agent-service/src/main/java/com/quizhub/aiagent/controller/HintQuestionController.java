package com.quizhub.aiagent.controller;

import com.quizhub.aiagent.application.HintQuestionService;
import com.quizhub.aiagent.dto.response.AIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class HintQuestionController {

    private final HintQuestionService service;

    @PostMapping("/hint-question/{id}")
    public ResponseEntity<AIResponse> hint(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                service.generateHint(id)
        );
    }
}