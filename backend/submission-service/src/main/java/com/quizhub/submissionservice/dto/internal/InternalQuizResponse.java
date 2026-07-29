package com.quizhub.submissionservice.dto.internal;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalQuizResponse {

    private UUID id;
    private UUID ownerId;
    private Integer totalMarks;
    private Integer durationMinutes;
    private String status;
}