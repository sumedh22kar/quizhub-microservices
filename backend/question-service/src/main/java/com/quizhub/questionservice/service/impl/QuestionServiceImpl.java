package com.quizhub.questionservice.service.impl;

import com.quizhub.questionservice.client.QuizServiceClient;
import com.quizhub.questionservice.dto.internal.InternalQuestionResponse;
import com.quizhub.questionservice.dto.request.CreateQuestionRequest;
import com.quizhub.questionservice.dto.request.UpdateQuestionRequest;
import com.quizhub.questionservice.dto.response.QuestionResponse;
import com.quizhub.questionservice.dto.response.QuizOwnerResponse;
import com.quizhub.questionservice.entity.Question;
import com.quizhub.questionservice.exception.AccessDeniedException;
import com.quizhub.questionservice.exception.ResourceNotFoundException;
import com.quizhub.questionservice.mapper.QuestionMapper;
import com.quizhub.questionservice.repository.QuestionRepository;
import com.quizhub.questionservice.security.UserContext;
import com.quizhub.questionservice.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final QuizServiceClient quizServiceClient;
    private final UserContext userContext;
    private final CacheManager cacheManager;

    @Override
    @CacheEvict(value = "questions", key = "#result.quizId")
    public QuestionResponse createQuestion(CreateQuestionRequest request) {
        verifyQuizOwnership(request.getQuizId());

        Question question = questionMapper.toEntity(request);
        if (question.getActive() == null) {
            question.setActive(true);
        }
        Question savedQuestion = questionRepository.save(question);
        return questionMapper.toResponse(savedQuestion);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionResponse getQuestionById(UUID id) {
        Question question = findQuestionById(id);
        return questionMapper.toResponse(question);
    }

    @Override
    @CacheEvict(value = "questions", key = "#result.quizId")
    public QuestionResponse updateQuestion(UUID id, UpdateQuestionRequest request) {
        Question question = findQuestionById(id);
        verifyQuizOwnership(question.getQuizId());

        questionMapper.updateEntity(request, question);
        Question updatedQuestion = questionRepository.save(question);
        return questionMapper.toResponse(updatedQuestion);
    }

    @Override
    public void deleteQuestion(UUID id) {
        Question question = findQuestionById(id);
        verifyQuizOwnership(question.getQuizId());
        UUID quizId = question.getQuizId();
        questionRepository.delete(question);
        if (cacheManager.getCache("questions") != null) {
            cacheManager.getCache("questions").evict(quizId);
        }
    }

    @Override
    @CacheEvict(value = "questions", key = "#result.quizId")
    public QuestionResponse activateQuestion(UUID id) {
        Question question = findQuestionById(id);
        verifyQuizOwnership(question.getQuizId());

        question.setActive(true);
        Question updatedQuestion = questionRepository.save(question);
        return questionMapper.toResponse(updatedQuestion);
    }

    @Override
    @CacheEvict(value = "questions", key = "#result.quizId")
    public QuestionResponse deactivateQuestion(UUID id) {
        Question question = findQuestionById(id);
        verifyQuizOwnership(question.getQuizId());

        question.setActive(false);
        Question updatedQuestion = questionRepository.save(question);
        return questionMapper.toResponse(updatedQuestion);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "questions", key = "#quizId")
    public List<QuestionResponse> getQuestionsByQuizId(UUID quizId) {
        List<Question> questions = questionRepository.findByQuizIdAndActiveTrue(quizId);
        return questions.stream()
                .map(questionMapper::toResponse)
                .toList();
    }

    private Question findQuestionById(UUID id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id));
    }

    private void verifyQuizOwnership(UUID quizId) {
        QuizOwnerResponse owner = quizServiceClient.getQuizOwner(quizId);

        String currentUserIdStr = userContext.getUserId();

        if (currentUserIdStr != null && !currentUserIdStr.isBlank()) {
            UUID currentUser = UUID.fromString(currentUserIdStr);
            if (!owner.getOwnerId().equals(currentUser)) {
                throw new AccessDeniedException("You cannot modify another user's quiz.");
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "questions", key = "#quizId")
    public List<InternalQuestionResponse> getInternalQuestions(UUID quizId) {

        return questionRepository.findByQuizIdAndActiveTrue(quizId)
                .stream()
                .map(question -> InternalQuestionResponse.builder()
                        .id(question.getId())
                        .quizId(question.getQuizId())
                        .correctAnswer(question.getCorrectAnswer())
                        .marks(question.getMarks())
                        .build())
                .toList();
    }
    @Override
    @Transactional(readOnly = true)
    public InternalQuestionResponse getInternalQuestion(UUID id) {

        Question question = findQuestionById(id);

        return questionMapper.toInternal(question);

    }
}
