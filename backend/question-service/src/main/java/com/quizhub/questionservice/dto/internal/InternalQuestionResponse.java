package com.quizhub.questionservice.dto.internal;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalQuestionResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    private UUID quizId;

    private String questionText;

    private String optionA;

    private String optionB;

    private String optionC;

    private String optionD;

    private String correctAnswer;

    private String explanation;

    private String difficulty;

    private String subject;

    private String topic;

    private Integer marks;
}
