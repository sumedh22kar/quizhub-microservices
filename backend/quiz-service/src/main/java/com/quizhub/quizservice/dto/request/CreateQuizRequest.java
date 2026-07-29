package com.quizhub.quizservice.dto.request;

import com.quizhub.quizservice.entity.enums.Difficulty;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateQuizRequest {

    @NotBlank(message = "Title is required.")
    @Size(max = 150)
    private String title;

    @Size(max = 2000)
    private String description;

    @NotNull(message = "Difficulty is required.")
    private Difficulty difficulty;

    @NotNull(message = "Duration is required.")
    @Min(1)
    @Max(300)
    private Integer durationMinutes;

    @NotNull(message = "Total marks are required.")
    @Min(1)
    private Integer totalMarks;

    private UUID ownerId;
}
