package com.quizhub.aiagent.dto;

import lombok.Data;

@Data
public class QuestionResponse {

    private Long id;

    private String questionText;

    private String optionA;

    private String optionB;

    private String optionC;

    private String optionD;

    private String correctAnswer;

    private String explanation;

    private String difficulty;

    private String subject;
}