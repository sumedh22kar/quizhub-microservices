package com.quizhub.quizservice.service.impl;

import com.quizhub.quizservice.dto.internal.InternalQuizResponse;
import com.quizhub.quizservice.dto.request.CreateQuizRequest;
import com.quizhub.quizservice.dto.request.UpdateQuizRequest;
import com.quizhub.quizservice.dto.response.QuizOwnerResponse;
import com.quizhub.quizservice.dto.response.QuizResponse;
import com.quizhub.quizservice.entity.Quiz;
import com.quizhub.quizservice.entity.enums.QuizStatus;
import com.quizhub.quizservice.exception.AccessDeniedException;
import com.quizhub.quizservice.exception.ResourceNotFoundException;
import com.quizhub.quizservice.mapper.QuizMapper;
import com.quizhub.quizservice.repository.QuizRepository;
import com.quizhub.quizservice.security.UserContext;
import com.quizhub.quizservice.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuizMapper quizMapper;
    private final UserContext userContext;

    @Override
    public QuizResponse createQuiz(CreateQuizRequest request) {
        UUID ownerId = getCurrentUserId();
        if (ownerId == null && request.getOwnerId() != null) {
            ownerId = request.getOwnerId();
        }
        if (ownerId == null) {
            ownerId = UUID.randomUUID();
        }

        Quiz quiz = Quiz.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .difficulty(request.getDifficulty())
                .durationMinutes(request.getDurationMinutes())
                .totalMarks(request.getTotalMarks())
                .status(QuizStatus.DRAFT)
                .ownerId(ownerId)
                .build();

        Quiz savedQuiz = quizRepository.save(quiz);

        return quizMapper.toResponse(savedQuiz);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "quiz", key = "#quizId")
    public QuizResponse getQuizById(UUID quizId) {

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Quiz not found."));

        return quizMapper.toResponse(quiz);
    }

    @Override
    public List<QuizResponse> getMyQuizzes() {
        UUID currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return List.of();
        }
        return quizRepository.findAll().stream()
                .filter(q -> currentUserId.equals(q.getOwnerId()))
                .map(quizMapper::toResponse)
                .toList();
    }

    @Override
    @CacheEvict(value = "quiz", key = "#quizId")
    public QuizResponse updateQuiz(UUID quizId,
                                   UpdateQuizRequest request) {

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Quiz not found."));

        verifyOwnership(quiz);

        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setDifficulty(request.getDifficulty());
        quiz.setDurationMinutes(request.getDurationMinutes());
        quiz.setTotalMarks(request.getTotalMarks());

        Quiz updatedQuiz = quizRepository.save(quiz);

        return quizMapper.toResponse(updatedQuiz);
    }

    @Override
    @CacheEvict(value = "quiz", key = "#quizId")
    public void deleteQuiz(UUID quizId) {

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Quiz not found."));

        verifyOwnership(quiz);

        quizRepository.delete(quiz);
    }

    @Override
    @CacheEvict(value = "quiz", key = "#quizId")
    public QuizResponse publishQuiz(UUID quizId) {

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Quiz not found."));

        verifyOwnership(quiz);

        quiz.setStatus(QuizStatus.PUBLISHED);

        Quiz updatedQuiz = quizRepository.save(quiz);

        return quizMapper.toResponse(updatedQuiz);
    }

    @Override
    @CacheEvict(value = "quiz", key = "#quizId")
    public QuizResponse archiveQuiz(UUID quizId) {

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Quiz not found."));

        verifyOwnership(quiz);

        quiz.setStatus(QuizStatus.ARCHIVED);

        Quiz updatedQuiz = quizRepository.save(quiz);

        return quizMapper.toResponse(updatedQuiz);
    }

    @Override
    public boolean quizExists(UUID quizId) {
        return quizRepository.existsById(quizId);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizOwnerResponse getQuizOwner(UUID quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found."));

        return new QuizOwnerResponse(
                quiz.getId(),
                quiz.getOwnerId()
        );
    }

    private void verifyOwnership(Quiz quiz) {
        UUID currentUserId = getCurrentUserId();
        if (currentUserId != null && !quiz.getOwnerId().equals(currentUserId)) {
            throw new AccessDeniedException(
                    "You are not authorized to modify this quiz."
            );
        }
    }

    private UUID getCurrentUserId() {
        String currentUserIdStr = null;
        try {
            if (userContext != null) {
                currentUserIdStr = userContext.getUserId();
            }
        } catch (Exception ignored) {
        }

        if (currentUserIdStr != null && !currentUserIdStr.isBlank()) {
            try {
                return UUID.fromString(currentUserIdStr);
            } catch (IllegalArgumentException ignored) {
            }
        }

        return null;
    }

    @Override
    @Cacheable(value = "quiz", key = "#quizId")
    public InternalQuizResponse getInternalQuiz(UUID quizId) {

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Quiz not found."));

        return InternalQuizResponse.builder()
                .id(quiz.getId())
                .ownerId(quiz.getOwnerId())
                .totalMarks(quiz.getTotalMarks())
                .durationMinutes(quiz.getDurationMinutes())
                .status(quiz.getStatus())
                .build();
    }
}