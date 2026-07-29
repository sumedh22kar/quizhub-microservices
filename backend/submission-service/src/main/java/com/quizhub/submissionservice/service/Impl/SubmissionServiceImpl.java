package com.quizhub.submissionservice.service.impl;

import com.quizhub.submissionservice.client.QuestionServiceClient;
import com.quizhub.submissionservice.client.QuizServiceClient;
import com.quizhub.submissionservice.client.ResultServiceClient;
import com.quizhub.submissionservice.dto.internal.GenerateResultRequest;
import com.quizhub.submissionservice.dto.internal.InternalQuestionResponse;
import com.quizhub.submissionservice.dto.internal.InternalQuizResponse;
import com.quizhub.submissionservice.dto.request.StartSubmissionRequest;
import com.quizhub.submissionservice.dto.request.SubmitAnswerRequest;
import com.quizhub.submissionservice.dto.request.SubmitQuizRequest;
import com.quizhub.submissionservice.dto.response.SubmissionResponse;
import com.quizhub.submissionservice.entity.Submission;
import com.quizhub.submissionservice.entity.SubmissionAnswer;
import com.quizhub.submissionservice.entity.enums.SubmissionStatus;
import com.quizhub.submissionservice.exception.AccessDeniedException;
import com.quizhub.submissionservice.exception.ResourceNotFoundException;
import com.quizhub.submissionservice.event.QuizSubmittedEvent;
import com.quizhub.submissionservice.kafka.QuizSubmittedProducer;
import com.quizhub.submissionservice.mapper.SubmissionMapper;
import com.quizhub.submissionservice.repository.SubmissionAnswerRepository;
import com.quizhub.submissionservice.repository.SubmissionRepository;
import com.quizhub.submissionservice.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final SubmissionAnswerRepository submissionAnswerRepository;
    private final QuizServiceClient quizServiceClient;
    private final QuestionServiceClient questionServiceClient;
    private final ResultServiceClient resultServiceClient;
    private final QuizSubmittedProducer quizSubmittedProducer;
    private final SubmissionMapper submissionMapper;

    @Override
    public SubmissionResponse startQuiz(StartSubmissionRequest request, UUID userId) {
        InternalQuizResponse quiz = quizServiceClient.getQuiz(request.getQuizId());
        if (quiz == null) {
            throw new ResourceNotFoundException("Quiz not found.");
        }

        if (!"PUBLISHED".equalsIgnoreCase(quiz.getStatus())) {
            throw new IllegalStateException("Quiz is not published.");
        }

        Optional<Submission> existingSubmission = submissionRepository
                .findByUserIdAndQuizIdAndStatus(userId, request.getQuizId(), SubmissionStatus.IN_PROGRESS);

        if (existingSubmission.isPresent()) {
            return submissionMapper.toResponse(existingSubmission.get());
        }

        Submission submission = Submission.builder()
                .quizId(request.getQuizId())
                .userId(userId)
                .totalMarks(quiz.getTotalMarks())
                .status(SubmissionStatus.IN_PROGRESS)
                .startedAt(Instant.now())
                .build();

        Submission savedSubmission = submissionRepository.save(submission);
        return submissionMapper.toResponse(savedSubmission);
    }

    @Override
    public SubmissionResponse submitQuiz(SubmitQuizRequest request, UUID userId) {
        // Step 1 — Fetch Submission
        Submission submission = submissionRepository.findById(request.getSubmissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found."));

        if (userId != null && !submission.getUserId().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to submit this quiz.");
        }

        if (submission.getStatus() != SubmissionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Submission is already submitted.");
        }

        // Step 2 — Fetch Quiz
        InternalQuizResponse quiz = quizServiceClient.getQuiz(submission.getQuizId());
        if (quiz == null) {
            throw new ResourceNotFoundException("Quiz not found.");
        }

        // Step 3 — Validate Quiz Status
        if (!"PUBLISHED".equalsIgnoreCase(quiz.getStatus())) {
            throw new IllegalStateException("Quiz is not published.");
        }

        // Step 4 — Fetch Questions
        List<InternalQuestionResponse> questions = questionServiceClient.getQuestions(submission.getQuizId());
        if (questions == null) {
            questions = List.of();
        }

        // Step 5 — Create Lookup Map
        Map<UUID, InternalQuestionResponse> questionMap = questions.stream()
                .collect(Collectors.toMap(
                        InternalQuestionResponse::getId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

        // Step 6 & 7 — Loop Through Submitted Answers & Compare Answers
        int score = 0;
        int correctAnswers = 0;
        int totalQuestions = questions.size();

        if (request.getAnswers() != null) {
            for (SubmitAnswerRequest answer : request.getAnswers()) {
                InternalQuestionResponse question = questionMap.get(answer.getQuestionId());
                if (question == null) {
                    continue;
                }

                boolean correct = question.getCorrectAnswer() != null
                        && question.getCorrectAnswer().equalsIgnoreCase(answer.getSelectedAnswer());

                if (correct) {
                    score += (question.getMarks() != null ? question.getMarks() : 0);
                    correctAnswers++;
                }

                // Step 8 — Save SubmissionAnswer
                SubmissionAnswer submissionAnswer = SubmissionAnswer.builder()
                        .submission(submission)
                        .questionId(question.getId())
                        .selectedAnswer(answer.getSelectedAnswer())
                        .correctAnswer(question.getCorrectAnswer())
                        .isCorrect(correct)
                        .marksAwarded(correct ? (double) (question.getMarks() != null ? question.getMarks() : 0) : 0.0)
                        .build();

                submission.addAnswer(submissionAnswer);
                submissionAnswerRepository.save(submissionAnswer);
            }
        }

        int wrongAnswers = Math.max(0, totalQuestions - correctAnswers);

        // Step 9 — Calculate Percentage
        double percentage = 0.0;
        if (quiz.getTotalMarks() != null && quiz.getTotalMarks() > 0) {
            percentage = (score * 100.0) / quiz.getTotalMarks();
        }

        // Step 10 — Update Submission
        submission.setScore((double) score);
        submission.setPercentage(percentage);
        submission.setSubmittedAt(Instant.now());
        submission.setStatus(SubmissionStatus.SUBMITTED);

        Submission savedSubmission = submissionRepository.save(submission);

        // Publish Kafka Event
        QuizSubmittedEvent event = QuizSubmittedEvent.builder()
                .submissionId(savedSubmission.getId())
                .quizId(savedSubmission.getQuizId())
                .userId(savedSubmission.getUserId())
                .score(savedSubmission.getScore())
                .percentage(savedSubmission.getPercentage())
                .totalQuestions(totalQuestions)
                .correctAnswers(correctAnswers)
                .wrongAnswers(wrongAnswers)
                .build();

        try {
            if (quizSubmittedProducer != null) {
                quizSubmittedProducer.sendQuizSubmittedEvent(event);
            }
        } catch (Exception e) {
            log.error("Failed to publish QuizSubmittedEvent for submissionId: {}", savedSubmission.getId(), e);
        }

        // Step 11 — Call Result Service via OpenFeign (Temporarily kept as fallback)
        try {
            if (resultServiceClient != null) {
                resultServiceClient.generateResult(
                        GenerateResultRequest.builder()
                                .submissionId(savedSubmission.getId())
                                .quizId(savedSubmission.getQuizId())
                                .userId(savedSubmission.getUserId())
                                .score(savedSubmission.getScore())
                                .percentage(savedSubmission.getPercentage())
                                .totalQuestions(totalQuestions)
                                .correctAnswers(correctAnswers)
                                .wrongAnswers(wrongAnswers)
                                .build()
                );
            }
        } catch (Exception e) {
            log.error("Failed to generate result in Result Service for submissionId: {}", savedSubmission.getId(), e);
        }

        // Step 12 — Return Response
        return submissionMapper.toResponse(savedSubmission);
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionResponse getSubmission(UUID submissionId, UUID userId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found."));

        if (userId != null && !submission.getUserId().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to view this submission.");
        }

        return submissionMapper.toResponse(submission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionResponse> getMySubmissions(UUID userId) {
        if (userId == null) {
            return List.of();
        }
        List<Submission> submissions = submissionRepository.findByUserId(userId);
        return submissionMapper.toResponseList(submissions);
    }
}
