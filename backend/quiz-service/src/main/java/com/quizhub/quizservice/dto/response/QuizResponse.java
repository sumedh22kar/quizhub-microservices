package com.quizhub.quizservice.dto.response;

import com.quizhub.quizservice.entity.enums.Difficulty;
import com.quizhub.quizservice.entity.enums.QuizStatus;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    private String title;

    private String description;

    private Difficulty difficulty;

    private QuizStatus status;

    private Integer durationMinutes;

    private Integer totalMarks;

    private UUID ownerId;

    private Instant createdAt;

    private Instant updatedAt;

}