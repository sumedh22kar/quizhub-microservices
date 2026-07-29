package com.quizhub.quizservice.service;

import com.quizhub.quizservice.dto.internal.InternalQuizResponse;
import com.quizhub.quizservice.dto.request.CreateQuizRequest;
import com.quizhub.quizservice.dto.request.UpdateQuizRequest;
import com.quizhub.quizservice.dto.response.QuizOwnerResponse;
import com.quizhub.quizservice.dto.response.QuizResponse;

import java.util.List;
import java.util.UUID;

public interface QuizService {

    QuizResponse createQuiz(CreateQuizRequest request);

    QuizResponse getQuizById(UUID quizId);

    List<QuizResponse> getMyQuizzes();

    QuizResponse updateQuiz(UUID quizId,
                            UpdateQuizRequest request);

    void deleteQuiz(UUID quizId);

    QuizResponse publishQuiz(UUID quizId);

    QuizResponse archiveQuiz(UUID quizId);

    boolean quizExists(UUID quizId);

    QuizOwnerResponse getQuizOwner(UUID quizId);

    InternalQuizResponse getInternalQuiz(UUID quizId);
}