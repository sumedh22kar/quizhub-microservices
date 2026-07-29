package com.quizhub.resultservice.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultResponse {
    private UUID id;
    private UUID submissionId;
    private UUID quizId;
    private UUID userId;
    private Double score;
    private Double percentage;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer wrongAnswers;
    private Boolean passed;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}
