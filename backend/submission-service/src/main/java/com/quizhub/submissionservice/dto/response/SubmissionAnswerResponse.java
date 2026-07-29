package com.quizhub.submissionservice.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionAnswerResponse {

    private UUID id;
    private UUID submissionId;
    private UUID questionId;
    private String selectedAnswer;
    private String correctAnswer;
    private Boolean isCorrect;
    private Double marksAwarded;
    private Instant createdAt;
    private Instant updatedAt;
}
