package com.quizhub.submissionservice.service;

import com.quizhub.submissionservice.client.QuestionServiceClient;
import com.quizhub.submissionservice.client.QuizServiceClient;
import com.quizhub.submissionservice.dto.internal.InternalQuestionResponse;
import com.quizhub.submissionservice.dto.internal.InternalQuizResponse;
import com.quizhub.submissionservice.dto.request.StartSubmissionRequest;
import com.quizhub.submissionservice.dto.request.SubmitAnswerRequest;
import com.quizhub.submissionservice.dto.request.SubmitQuizRequest;
import com.quizhub.submissionservice.dto.response.SubmissionResponse;
import com.quizhub.submissionservice.entity.Submission;
import com.quizhub.submissionservice.entity.SubmissionAnswer;
import com.quizhub.submissionservice.entity.enums.SubmissionStatus;
import com.quizhub.submissionservice.exception.ResourceNotFoundException;
import com.quizhub.submissionservice.mapper.SubmissionMapper;
import com.quizhub.submissionservice.repository.SubmissionAnswerRepository;
import com.quizhub.submissionservice.repository.SubmissionRepository;
import com.quizhub.submissionservice.service.impl.SubmissionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceImplTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private SubmissionAnswerRepository submissionAnswerRepository;

    @Mock
    private QuizServiceClient quizServiceClient;

    @Mock
    private QuestionServiceClient questionServiceClient;

    @Mock
    private com.quizhub.submissionservice.client.ResultServiceClient resultServiceClient;

    @Mock
    private SubmissionMapper submissionMapper;

    @InjectMocks
    private SubmissionServiceImpl submissionService;

    private UUID userId;
    private UUID quizId;
    private UUID submissionId;
    private UUID q1Id;
    private UUID q2Id;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        quizId = UUID.randomUUID();
        submissionId = UUID.randomUUID();
        q1Id = UUID.randomUUID();
        q2Id = UUID.randomUUID();
    }

    @Test
    @DisplayName("startQuiz — Should start quiz submission successfully")
    void testStartQuiz_Success() {
        StartSubmissionRequest request = StartSubmissionRequest.builder()
                .quizId(quizId)
                .build();

        InternalQuizResponse quiz = InternalQuizResponse.builder()
                .id(quizId)
                .ownerId(UUID.randomUUID())
                .totalMarks(100)
                .durationMinutes(30)
                .status("PUBLISHED")
                .build();

        when(quizServiceClient.getQuiz(quizId)).thenReturn(quiz);
        when(submissionRepository.findByUserIdAndQuizIdAndStatus(userId, quizId, SubmissionStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> {
            Submission s = invocation.getArgument(0);
            s.setId(submissionId);
            return s;
        });

        SubmissionResponse expectedResponse = SubmissionResponse.builder()
                .id(submissionId)
                .quizId(quizId)
                .userId(userId)
                .status(SubmissionStatus.IN_PROGRESS)
                .totalMarks(100)
                .build();

        when(submissionMapper.toResponse(any(Submission.class))).thenReturn(expectedResponse);

        SubmissionResponse response = submissionService.startQuiz(request, userId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(submissionId);
        verify(submissionRepository, times(1)).save(any(Submission.class));
    }

    @Test
    @DisplayName("submitQuiz — Should calculate score and submit quiz successfully")
    void testSubmitQuiz_Success() {
        Submission submission = Submission.builder()
                .id(submissionId)
                .quizId(quizId)
                .userId(userId)
                .totalMarks(100)
                .status(SubmissionStatus.IN_PROGRESS)
                .startedAt(Instant.now().minusSeconds(600))
                .build();

        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));

        InternalQuizResponse quiz = InternalQuizResponse.builder()
                .id(quizId)
                .ownerId(UUID.randomUUID())
                .totalMarks(100)
                .durationMinutes(30)
                .status("PUBLISHED")
                .build();
        when(quizServiceClient.getQuiz(quizId)).thenReturn(quiz);

        InternalQuestionResponse q1 = InternalQuestionResponse.builder()
                .id(q1Id)
                .quizId(quizId)
                .correctAnswer("A")
                .marks(10)
                .build();

        InternalQuestionResponse q2 = InternalQuestionResponse.builder()
                .id(q2Id)
                .quizId(quizId)
                .correctAnswer("C")
                .marks(15)
                .build();

        when(questionServiceClient.getQuestions(quizId)).thenReturn(List.of(q1, q2));

        SubmitQuizRequest submitRequest = SubmitQuizRequest.builder()
                .submissionId(submissionId)
                .answers(List.of(
                        SubmitAnswerRequest.builder().questionId(q1Id).selectedAnswer("A").build(), // Correct (10 marks)
                        SubmitAnswerRequest.builder().questionId(q2Id).selectedAnswer("B").build()  // Incorrect (0 marks)
                ))
                .build();

        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SubmissionResponse expectedResponse = SubmissionResponse.builder()
                .id(submissionId)
                .quizId(quizId)
                .userId(userId)
                .status(SubmissionStatus.SUBMITTED)
                .score(10.0)
                .totalMarks(100)
                .percentage(10.0)
                .build();

        when(submissionMapper.toResponse(any(Submission.class))).thenReturn(expectedResponse);

        SubmissionResponse response = submissionService.submitQuiz(submitRequest, userId);

        assertThat(response).isNotNull();
        assertThat(response.getScore()).isEqualTo(10.0);
        assertThat(response.getPercentage()).isEqualTo(10.0);
        assertThat(response.getStatus()).isEqualTo(SubmissionStatus.SUBMITTED);

        verify(submissionAnswerRepository, times(2)).save(any(SubmissionAnswer.class));
        verify(submissionRepository, times(1)).save(submission);
    }

    @Test
    @DisplayName("submitQuiz — Should throw Exception if submission not found")
    void testSubmitQuiz_NotFound() {
        SubmitQuizRequest request = SubmitQuizRequest.builder()
                .submissionId(submissionId)
                .answers(List.of())
                .build();

        when(submissionRepository.findById(submissionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> submissionService.submitQuiz(request, userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Submission not found.");
    }
}
