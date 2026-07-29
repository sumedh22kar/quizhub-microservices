package com.quizhub.submissionservice.dto.internal;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateResultRequest {

    private UUID submissionId;
    private UUID quizId;
    private UUID userId;

    private Double score;
    private Double percentage;

    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer wrongAnswers;
}
