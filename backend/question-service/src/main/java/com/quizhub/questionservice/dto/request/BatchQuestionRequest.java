package com.quizhub.questionservice.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchQuestionRequest {

    @NotEmpty(message = "Question IDs cannot be empty")
    private List<UUID> questionIds;

}