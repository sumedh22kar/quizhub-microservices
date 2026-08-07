package com.quizhub.aiagent.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSubmissionResponse {

    private String answer;

    private String model;

    private Long responseTime;

}