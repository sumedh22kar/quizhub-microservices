package com.quizhub.aiagent.dto.response;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionAnalysisResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String difficulty;

    private String estimatedTime;

    private List<String> concepts;

    private List<String> commonMistakes;

    private List<String> recommendedTopics;
}