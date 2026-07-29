package com.quizhub.resultservice.service;

import com.quizhub.resultservice.dto.internal.GenerateResultRequest;
import com.quizhub.resultservice.dto.response.ResultResponse;
import com.quizhub.resultservice.event.QuizSubmittedEvent;

import java.util.List;
import java.util.UUID;

public interface ResultService {

    ResultResponse getResult(UUID id);

    List<ResultResponse> getMyResults();

    List<ResultResponse> getQuizResults(UUID quizId);

    List<ResultResponse> getLeaderboard(UUID quizId);

    ResultResponse generateResult(GenerateResultRequest request);

    void generateResult(QuizSubmittedEvent event);
}
