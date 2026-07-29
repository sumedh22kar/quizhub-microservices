package com.quizhub.resultservice.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardEntryResponse {
    private Integer rank;
    private UUID userId;
    private UUID submissionId;
    private Double score;
    private Double percentage;
    private LocalDateTime completedAt;
}
