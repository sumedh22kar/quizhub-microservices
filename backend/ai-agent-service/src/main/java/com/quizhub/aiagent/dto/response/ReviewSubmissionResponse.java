package com.quizhub.aiagent.dto.response;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSubmissionResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String answer;

    private String model;

    private Long responseTime;

}