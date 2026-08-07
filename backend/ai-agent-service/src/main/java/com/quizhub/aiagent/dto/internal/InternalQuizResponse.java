package com.quizhub.aiagent.dto.internal;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternalQuizResponse implements Serializable {

    private UUID id;

    private UUID ownerId;

    private Integer totalMarks;

    private Integer durationMinutes;

    private String status;

}