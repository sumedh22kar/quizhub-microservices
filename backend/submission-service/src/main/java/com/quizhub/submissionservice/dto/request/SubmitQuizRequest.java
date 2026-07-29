package com.quizhub.submissionservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitQuizRequest {

    private UUID submissionId;

    @NotNull(message = "Answers list cannot be null")
    @Valid
    private List<SubmitAnswerRequest> answers;
}
