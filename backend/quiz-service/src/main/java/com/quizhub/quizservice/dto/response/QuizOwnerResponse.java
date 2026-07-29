package com.quizhub.quizservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizOwnerResponse {

    private UUID quizId;
    private UUID ownerId;

}
