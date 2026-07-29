package com.quizhub.resultservice.service.impl;

import com.quizhub.resultservice.dto.internal.GenerateResultRequest;
import com.quizhub.resultservice.dto.response.ResultResponse;
import com.quizhub.resultservice.entity.Result;
import com.quizhub.resultservice.event.QuizSubmittedEvent;
import com.quizhub.resultservice.exception.AccessDeniedException;
import com.quizhub.resultservice.exception.ResourceNotFoundException;
import com.quizhub.resultservice.mapper.ResultMapper;
import com.quizhub.resultservice.repository.ResultRepository;
import com.quizhub.resultservice.security.UserContext;
import com.quizhub.resultservice.service.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ResultServiceImpl implements ResultService {

    private final ResultRepository resultRepository;
    private final ResultMapper resultMapper;
    private final UserContext userContext;

    @Override
    @Transactional(readOnly = true)
    public ResultResponse getResult(UUID id) {
        Result result = resultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Result not found with ID: " + id));

        UUID currentUserId = getCurrentUserId();
        if (currentUserId != null && !result.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not authorized to view this result.");
        }

        return resultMapper.toResponse(result);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResultResponse> getMyResults() {
        UUID currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return List.of();
        }
        List<Result> results = resultRepository.findByUserId(currentUserId);
        return resultMapper.toResponseList(results);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResultResponse> getQuizResults(UUID quizId) {
        List<Result> results = resultRepository.findByQuizId(quizId);
        return resultMapper.toResponseList(results);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResultResponse> getLeaderboard(UUID quizId) {
        List<Result> sortedResults = resultRepository.findByQuizIdOrderByScoreDesc(quizId);
        return resultMapper.toResponseList(sortedResults);
    }

    @Override
    public ResultResponse generateResult(GenerateResultRequest request) {
        Optional<Result> existingResult = resultRepository.findBySubmissionId(request.getSubmissionId());
        if (existingResult.isPresent()) {
            return resultMapper.toResponse(existingResult.get());
        }

        boolean passed = request.getPercentage() != null && request.getPercentage() >= 40.0;

        Result result = Result.builder()
                .submissionId(request.getSubmissionId())
                .quizId(request.getQuizId())
                .userId(request.getUserId())
                .score(request.getScore() != null ? request.getScore() : 0.0)
                .percentage(request.getPercentage() != null ? request.getPercentage() : 0.0)
                .totalQuestions(request.getTotalQuestions() != null ? request.getTotalQuestions() : 0)
                .correctAnswers(request.getCorrectAnswers() != null ? request.getCorrectAnswers() : 0)
                .wrongAnswers(request.getWrongAnswers() != null ? request.getWrongAnswers() : 0)
                .passed(passed)
                .completedAt(LocalDateTime.now())
                .build();

        Result savedResult = resultRepository.save(result);
        return resultMapper.toResponse(savedResult);
    }

    @Override
    public void generateResult(QuizSubmittedEvent event) {
        Optional<Result> existingResult = resultRepository.findBySubmissionId(event.getSubmissionId());
        if (existingResult.isPresent()) {
            return;
        }

        boolean passed = event.getPercentage() != null && event.getPercentage() >= 40.0;

        Result result = Result.builder()
                .submissionId(event.getSubmissionId())
                .quizId(event.getQuizId())
                .userId(event.getUserId())
                .score(event.getScore() != null ? event.getScore() : 0.0)
                .percentage(event.getPercentage() != null ? event.getPercentage() : 0.0)
                .totalQuestions(event.getTotalQuestions() != null ? event.getTotalQuestions() : 0)
                .correctAnswers(event.getCorrectAnswers() != null ? event.getCorrectAnswers() : 0)
                .wrongAnswers(event.getWrongAnswers() != null ? event.getWrongAnswers() : 0)
                .passed(passed)
                .completedAt(LocalDateTime.now())
                .build();

        resultRepository.save(result);
    }

    private UUID getCurrentUserId() {
        if (userContext != null && userContext.getUserId() != null) {
            return userContext.getUserId();
        }
        return null;
    }
}
