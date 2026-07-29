package com.quizhub.quizservice.dto.internal;

import com.quizhub.quizservice.entity.enums.QuizStatus;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalQuizResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    private UUID ownerId;

    private Integer totalMarks;

    private Integer durationMinutes;

    private QuizStatus status;

}
