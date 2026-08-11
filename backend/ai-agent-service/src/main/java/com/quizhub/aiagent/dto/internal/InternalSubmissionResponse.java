package com.quizhub.aiagent.dto.internal;

import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternalSubmissionResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    private UUID quizId;

    private UUID userId;

    private String status;

    private Double score;

    private Integer totalMarks;

    private Double percentage;

    private Instant startedAt;

    private Instant submittedAt;

    private List<InternalSubmissionAnswerResponse> answers;

}