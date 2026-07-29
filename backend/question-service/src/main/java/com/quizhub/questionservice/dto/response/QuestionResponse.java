package com.quizhub.questionservice.dto.response;

import com.quizhub.questionservice.entity.enums.QuestionType;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    private UUID quizId;

    private String questionText;

    private QuestionType questionType;

    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

    private String correctAnswer;

    private Integer marks;

    private Boolean active;
}
