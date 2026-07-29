package com.quizhub.resultservice.service;

import com.quizhub.resultservice.dto.response.ResultResponse;
import com.quizhub.resultservice.entity.Result;
import com.quizhub.resultservice.exception.AccessDeniedException;
import com.quizhub.resultservice.exception.ResourceNotFoundException;
import com.quizhub.resultservice.mapper.ResultMapper;
import com.quizhub.resultservice.repository.ResultRepository;
import com.quizhub.resultservice.security.UserContext;
import com.quizhub.resultservice.service.impl.ResultServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResultServiceImplTest {

    @Mock
    private ResultRepository resultRepository;

    @Mock
    private ResultMapper resultMapper;

    @Mock
    private UserContext userContext;

    @InjectMocks
    private ResultServiceImpl resultService;

    private UUID userId;
    private UUID quizId;
    private UUID submissionId;
    private UUID resultId;
    private Result sampleResult;
    private ResultResponse sampleResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        quizId = UUID.randomUUID();
        submissionId = UUID.randomUUID();
        resultId = UUID.randomUUID();

        sampleResult = Result.builder()
                .id(resultId)
                .submissionId(submissionId)
                .quizId(quizId)
                .userId(userId)
                .score(80.0)
                .percentage(80.0)
                .totalQuestions(10)
                .correctAnswers(8)
                .wrongAnswers(2)
                .passed(true)
                .completedAt(LocalDateTime.now())
                .build();

        sampleResponse = ResultResponse.builder()
                .id(resultId)
                .submissionId(submissionId)
                .quizId(quizId)
                .userId(userId)
                .score(80.0)
                .percentage(80.0)
                .totalQuestions(10)
                .correctAnswers(8)
                .wrongAnswers(2)
                .passed(true)
                .completedAt(sampleResult.getCompletedAt())
                .build();
    }

    @Test
    @DisplayName("getMyResults - Should return user's results")
    void getMyResults_Success() {
        when(userContext.getUserId()).thenReturn(userId);
        when(resultRepository.findByUserId(userId)).thenReturn(List.of(sampleResult));
        when(resultMapper.toResponseList(List.of(sampleResult))).thenReturn(List.of(sampleResponse));

        List<ResultResponse> results = resultService.getMyResults();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(resultId);
        verify(resultRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("getResult - Should return result when authorized")
    void getResult_Success() {
        when(userContext.getUserId()).thenReturn(userId);
        when(resultRepository.findById(resultId)).thenReturn(Optional.of(sampleResult));
        when(resultMapper.toResponse(sampleResult)).thenReturn(sampleResponse);

        ResultResponse response = resultService.getResult(resultId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(resultId);
    }

    @Test
    @DisplayName("getResult - Should throw AccessDeniedException when unauthorized user")
    void getResult_Unauthorized() {
        UUID otherUser = UUID.randomUUID();
        when(userContext.getUserId()).thenReturn(otherUser);
        when(resultRepository.findById(resultId)).thenReturn(Optional.of(sampleResult));

        assertThatThrownBy(() -> resultService.getResult(resultId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("getResult - Should throw ResourceNotFoundException when result not found")
    void getResult_NotFound() {
        when(resultRepository.findById(resultId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resultService.getResult(resultId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getLeaderboard - Should return ordered leaderboard by score")
    void getLeaderboard_Success() {
        when(resultRepository.findByQuizIdOrderByScoreDesc(quizId)).thenReturn(List.of(sampleResult));
        when(resultMapper.toResponseList(List.of(sampleResult))).thenReturn(List.of(sampleResponse));

        List<ResultResponse> leaderboard = resultService.getLeaderboard(quizId);

        assertThat(leaderboard).hasSize(1);
        assertThat(leaderboard.get(0).getScore()).isEqualTo(80.0);
    }

    @Test
    @DisplayName("generateResult - Should save result")
    void generateResult_Success() {
        com.quizhub.resultservice.dto.internal.GenerateResultRequest request = com.quizhub.resultservice.dto.internal.GenerateResultRequest.builder()
                .submissionId(submissionId)
                .quizId(quizId)
                .userId(userId)
                .score(80.0)
                .percentage(80.0)
                .totalQuestions(10)
                .correctAnswers(8)
                .wrongAnswers(2)
                .build();

        when(resultRepository.save(any(Result.class))).thenReturn(sampleResult);
        when(resultMapper.toResponse(sampleResult)).thenReturn(sampleResponse);

        ResultResponse response = resultService.generateResult(request);

        assertThat(response).isNotNull();
        verify(resultRepository, times(1)).save(any(Result.class));
    }
}
