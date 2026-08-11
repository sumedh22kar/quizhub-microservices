package com.quizhub.aiagent.dto.internal;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternalSubmissionAnswerResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    private UUID questionId;

    private String selectedAnswer;

    private String correctAnswer;

    private Boolean isCorrect;

    private Double marksAwarded;

}