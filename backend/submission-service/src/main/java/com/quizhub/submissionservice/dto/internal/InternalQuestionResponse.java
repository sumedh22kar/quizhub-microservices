package com.quizhub.submissionservice.dto.internal;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalQuestionResponse {

    private UUID id;
    private UUID quizId;
    private String correctAnswer;
    private Integer marks;
}