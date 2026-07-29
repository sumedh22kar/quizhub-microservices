package com.quizhub.submissionservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartSubmissionRequest {

    @NotNull(message = "Quiz ID is required")
    private UUID quizId;
}
