package com.quizhub.aiagent.dto.response;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyPlanResponse {

    private List<String> strengths;

    private List<String> weaknesses;

    private List<String> recommendedTopics;

    private List<String> nextSteps;

    private String summary;

}