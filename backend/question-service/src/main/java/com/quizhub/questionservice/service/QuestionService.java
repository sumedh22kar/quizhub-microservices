package com.quizhub.questionservice.service;

import com.quizhub.questionservice.dto.internal.InternalQuestionResponse;
import com.quizhub.questionservice.dto.request.CreateQuestionRequest;
import com.quizhub.questionservice.dto.request.UpdateQuestionRequest;
import com.quizhub.questionservice.dto.response.QuestionResponse;

import java.util.List;
import java.util.UUID;

public interface QuestionService {

    QuestionResponse createQuestion(CreateQuestionRequest request);

    QuestionResponse getQuestionById(UUID id);

    QuestionResponse updateQuestion(UUID id, UpdateQuestionRequest request);

    void deleteQuestion(UUID id);

    QuestionResponse activateQuestion(UUID id);

    QuestionResponse deactivateQuestion(UUID id);

    List<QuestionResponse> getQuestionsByQuizId(UUID quizId);

    List<InternalQuestionResponse> getInternalQuestions(UUID quizId);

    public InternalQuestionResponse getInternalQuestion(UUID id);
}