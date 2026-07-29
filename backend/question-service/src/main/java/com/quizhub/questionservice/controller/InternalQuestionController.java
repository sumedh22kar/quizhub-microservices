package com.quizhub.questionservice.controller;

import com.quizhub.questionservice.dto.internal.InternalQuestionResponse;
import com.quizhub.questionservice.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/questions")
@RequiredArgsConstructor
public class InternalQuestionController {

    private final QuestionService questionService;

    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<List<InternalQuestionResponse>> getQuestions(
            @PathVariable UUID quizId) {

        return ResponseEntity.ok(
                questionService.getInternalQuestions(quizId)
        );
    }
}