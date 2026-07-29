package com.quizhub.quizservice.dto.request;

import com.quizhub.quizservice.entity.enums.Difficulty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateQuizRequest {

    @NotBlank(message = "Title is required.")
    @Size(max = 150, message = "Title cannot exceed 150 characters.")
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters.")
    private String description;

    @NotNull(message = "Difficulty is required.")
    private Difficulty difficulty;

    @NotNull(message = "Duration is required.")
    @Min(value = 1, message = "Duration must be at least 1 minute.")
    @Max(value = 300, message = "Duration cannot exceed 300 minutes.")
    private Integer durationMinutes;

    @NotNull(message = "Total marks are required.")
    @Min(value = 1, message = "Total marks must be at least 1.")
    private Integer totalMarks;
}