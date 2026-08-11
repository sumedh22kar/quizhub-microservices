package com.quizhub.aiagent.controller;

import com.quizhub.aiagent.application.study.StudyPlanService;
import com.quizhub.aiagent.dto.response.ReviewSubmissionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    @Value("${spring.ai.ollama.chat.options.model:qwen3:8b}")
    private String modelName;

    @PostMapping("/study-plan/{submissionId}")
    public ResponseEntity<ReviewSubmissionResponse> getStudyPlan(
            @PathVariable UUID submissionId
    ) {

        long start = System.currentTimeMillis();

        String answer =
                studyPlanService.generateStudyPlan(submissionId);

        long end = System.currentTimeMillis();

        return ResponseEntity.ok(
                ReviewSubmissionResponse.builder()
                        .answer(answer)
                        .model(modelName)
                        .responseTime(end - start)
                        .build()
        );
    }
}