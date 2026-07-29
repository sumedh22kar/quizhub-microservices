package com.quizhub.submissionservice.dto.internal;

import com.quizhub.submissionservice.entity.enums.SubmissionStatus;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternalSubmissionResponse {
    private UUID id;
    private UUID quizId;
    private UUID userId;
    private SubmissionStatus status;
    private Double score;
    private Integer totalMarks;
    private Double percentage;
    private Instant startedAt;
    private Instant submittedAt;
    private List<InternalSubmissionAnswerResponse> answers;
}
