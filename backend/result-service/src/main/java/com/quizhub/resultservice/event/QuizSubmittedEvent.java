package com.quizhub.resultservice.event;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmittedEvent {

    private UUID submissionId;
    private UUID quizId;
    private UUID userId;

    private Double score;
    private Double percentage;

    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer wrongAnswers;
}
