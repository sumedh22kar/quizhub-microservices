package com.quizhub.questionservice.service;

import com.quizhub.questionservice.client.QuizServiceClient;
import com.quizhub.questionservice.dto.request.CreateQuestionRequest;
import com.quizhub.questionservice.dto.request.UpdateQuestionRequest;
import com.quizhub.questionservice.dto.response.QuestionResponse;
import com.quizhub.questionservice.dto.response.QuizOwnerResponse;
import com.quizhub.questionservice.entity.Question;
import com.quizhub.questionservice.entity.enums.QuestionType;
import com.quizhub.questionservice.exception.AccessDeniedException;
import com.quizhub.questionservice.exception.ResourceNotFoundException;
import com.quizhub.questionservice.mapper.QuestionMapper;
import com.quizhub.questionservice.repository.QuestionRepository;
import com.quizhub.questionservice.service.impl.QuestionServiceImpl;
import com.quizhub.questionservice.security.UserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceImplTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionMapper questionMapper;

    @Mock
    private QuizServiceClient quizServiceClient;

    @Mock
    private UserContext userContext;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private QuestionServiceImpl questionService;

    private UUID questionId;
    private UUID quizId;
    private UUID ownerId;
    private Question question;
    private CreateQuestionRequest createRequest;
    private UpdateQuestionRequest updateRequest;
    private QuestionResponse response;
    private QuizOwnerResponse ownerResponse;

    @BeforeEach
    void setUp() {
        questionId = UUID.randomUUID();
        quizId = UUID.randomUUID();
        ownerId = UUID.randomUUID();

        question = Question.builder()
                .id(questionId)
                .quizId(quizId)
                .questionText("What is Spring Boot?")
                .questionType(QuestionType.MCQ)
                .optionA("Framework")
                .optionB("Library")
                .correctAnswer("Framework")
                .marks(5)
                .active(true)
                .build();

        createRequest = CreateQuestionRequest.builder()
                .quizId(quizId)
                .questionText("What is Spring Boot?")
                .questionType(QuestionType.MCQ)
                .optionA("Framework")
                .optionB("Library")
                .correctAnswer("Framework")
                .marks(5)
                .build();

        updateRequest = UpdateQuestionRequest.builder()
                .quizId(quizId)
                .questionText("What is Spring Framework?")
                .questionType(QuestionType.MCQ)
                .optionA("Framework")
                .optionB("Library")
                .correctAnswer("Framework")
                .marks(10)
                .build();

        response = QuestionResponse.builder()
                .id(questionId)
                .quizId(quizId)
                .questionText("What is Spring Boot?")
                .questionType(QuestionType.MCQ)
                .optionA("Framework")
                .optionB("Library")
                .correctAnswer("Framework")
                .marks(5)
                .active(true)
                .build();

        ownerResponse = new QuizOwnerResponse(quizId, ownerId);
    }

    @Test
    void createQuestion_Success() {
        when(userContext.getUserId()).thenReturn(ownerId.toString());
        when(quizServiceClient.getQuizOwner(quizId)).thenReturn(ownerResponse);
        when(questionMapper.toEntity(any())).thenReturn(question);
        when(questionRepository.save(any())).thenReturn(question);
        when(questionMapper.toResponse(any())).thenReturn(response);

        QuestionResponse result = questionService.createQuestion(createRequest);

        assertNotNull(result);
        assertEquals(questionId, result.getId());
        verify(quizServiceClient, times(1)).getQuizOwner(quizId);
        verify(questionRepository, times(1)).save(any());
    }

    @Test
    void createQuestion_OtherUserQuiz_ThrowsAccessDenied() {
        UUID otherUser = UUID.randomUUID();
        when(userContext.getUserId()).thenReturn(otherUser.toString());
        when(quizServiceClient.getQuizOwner(quizId)).thenReturn(ownerResponse);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> questionService.createQuestion(createRequest));

        assertEquals("You cannot modify another user's quiz.", ex.getMessage());
        verify(questionRepository, never()).save(any());
    }

    @Test
    void createQuestion_QuizNotFound_ThrowsException() {
        when(quizServiceClient.getQuizOwner(quizId))
                .thenThrow(new ResourceNotFoundException("Quiz not found with id: " + quizId));

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> questionService.createQuestion(createRequest));

        assertEquals("Quiz not found with id: " + quizId, ex.getMessage());
        verify(questionRepository, never()).save(any());
    }

    @Test
    void getQuestionById_Success() {
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
        when(questionMapper.toResponse(question)).thenReturn(response);

        QuestionResponse result = questionService.getQuestionById(questionId);

        assertNotNull(result);
        assertEquals(questionId, result.getId());
    }

    @Test
    void getQuestionById_NotFound_ThrowsException() {
        when(questionRepository.findById(questionId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> questionService.getQuestionById(questionId));
    }

    @Test
    void updateQuestion_Success() {
        when(userContext.getUserId()).thenReturn(ownerId.toString());
        when(quizServiceClient.getQuizOwner(quizId)).thenReturn(ownerResponse);
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
        when(questionRepository.save(question)).thenReturn(question);
        when(questionMapper.toResponse(question)).thenReturn(response);

        QuestionResponse result = questionService.updateQuestion(questionId, updateRequest);

        assertNotNull(result);
        verify(questionMapper, times(1)).updateEntity(updateRequest, question);
        verify(questionRepository, times(1)).save(question);
    }

    @Test
    void deleteQuestion_Success() {
        when(userContext.getUserId()).thenReturn(ownerId.toString());
        when(quizServiceClient.getQuizOwner(quizId)).thenReturn(ownerResponse);
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
        when(cacheManager.getCache("questions")).thenReturn(cache);

        questionService.deleteQuestion(questionId);

        verify(questionRepository, times(1)).delete(question);
        verify(cache, times(1)).evict(quizId);
    }

    @Test
    void activateQuestion_Success() {
        question.setActive(false);
        when(userContext.getUserId()).thenReturn(ownerId.toString());
        when(quizServiceClient.getQuizOwner(quizId)).thenReturn(ownerResponse);
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
        when(questionRepository.save(question)).thenReturn(question);
        when(questionMapper.toResponse(question)).thenReturn(response);

        QuestionResponse result = questionService.activateQuestion(questionId);

        assertNotNull(result);
        assertTrue(question.getActive());
        verify(questionRepository, times(1)).save(question);
    }

    @Test
    void deactivateQuestion_Success() {
        when(userContext.getUserId()).thenReturn(ownerId.toString());
        when(quizServiceClient.getQuizOwner(quizId)).thenReturn(ownerResponse);
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
        when(questionRepository.save(question)).thenReturn(question);
        when(questionMapper.toResponse(question)).thenReturn(response);

        QuestionResponse result = questionService.deactivateQuestion(questionId);

        assertNotNull(result);
        assertFalse(question.getActive());
        verify(questionRepository, times(1)).save(question);
    }

    @Test
    void getQuestionsByQuizId_Success() {
        when(questionRepository.findByQuizIdAndActiveTrue(quizId)).thenReturn(List.of(question));
        when(questionMapper.toResponse(question)).thenReturn(response);

        List<QuestionResponse> result = questionService.getQuestionsByQuizId(quizId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(questionId, result.get(0).getId());
    }
}
