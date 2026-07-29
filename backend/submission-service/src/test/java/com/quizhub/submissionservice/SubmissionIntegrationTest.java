package com.quizhub.submissionservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizhub.submissionservice.client.QuestionServiceClient;
import com.quizhub.submissionservice.client.QuizServiceClient;
import com.quizhub.submissionservice.dto.internal.InternalQuestionResponse;
import com.quizhub.submissionservice.dto.internal.InternalQuizResponse;
import com.quizhub.submissionservice.dto.request.StartSubmissionRequest;
import com.quizhub.submissionservice.dto.request.SubmitAnswerRequest;
import com.quizhub.submissionservice.dto.request.SubmitQuizRequest;
import com.quizhub.submissionservice.entity.Submission;
import com.quizhub.submissionservice.entity.SubmissionAnswer;
import com.quizhub.submissionservice.entity.enums.SubmissionStatus;
import com.quizhub.submissionservice.repository.SubmissionAnswerRepository;
import com.quizhub.submissionservice.repository.SubmissionRepository;
import com.quizhub.submissionservice.kafka.QuizSubmittedProducer;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SubmissionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private SubmissionAnswerRepository submissionAnswerRepository;

    @MockitoBean
    private QuizServiceClient quizServiceClient;

    @MockitoBean
    private QuestionServiceClient questionServiceClient;

    @MockitoBean
    private com.quizhub.submissionservice.client.ResultServiceClient resultServiceClient;

    @MockitoBean
    private QuizSubmittedProducer quizSubmittedProducer;

    private UUID userId;
    private UUID quizId;

    @BeforeEach
    void setUp() {
        submissionAnswerRepository.deleteAll();
        submissionRepository.deleteAll();
        userId = UUID.randomUUID();
        quizId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Test 1 — Start Submission")
    void test1_StartSubmission() throws Exception {
        InternalQuizResponse quiz = InternalQuizResponse.builder()
                .id(quizId)
                .ownerId(UUID.randomUUID())
                .totalMarks(100)
                .durationMinutes(30)
                .status("PUBLISHED")
                .build();
        when(quizServiceClient.getQuiz(quizId)).thenReturn(quiz);

        StartSubmissionRequest startRequest = StartSubmissionRequest.builder()
                .quizId(quizId)
                .build();

        mockMvc.perform(post("/api/v1/submissions/start")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.quizId").value(quizId.toString()))
                .andExpect(jsonPath("$.data.userId").value(userId.toString()));
    }

    @Test
    @DisplayName("Test 2 & Test 3 — Submit Quiz and Database Verification")
    void test2_SubmitQuiz_And_DatabaseVerification() throws Exception {
        InternalQuizResponse quiz = InternalQuizResponse.builder()
                .id(quizId)
                .ownerId(UUID.randomUUID())
                .totalMarks(50)
                .durationMinutes(30)
                .status("PUBLISHED")
                .build();
        when(quizServiceClient.getQuiz(quizId)).thenReturn(quiz);

        UUID q1Id = UUID.randomUUID();
        UUID q2Id = UUID.randomUUID();

        InternalQuestionResponse q1 = InternalQuestionResponse.builder()
                .id(q1Id).quizId(quizId).correctAnswer("A").marks(20).build();
        InternalQuestionResponse q2 = InternalQuestionResponse.builder()
                .id(q2Id).quizId(quizId).correctAnswer("C").marks(30).build();

        when(questionServiceClient.getQuestions(quizId)).thenReturn(List.of(q1, q2));

        // Start submission
        StartSubmissionRequest startRequest = StartSubmissionRequest.builder().quizId(quizId).build();
        MvcResult startResult = mockMvc.perform(post("/api/v1/submissions/start")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode startJson = objectMapper.readTree(startResult.getResponse().getContentAsString());
        UUID submissionId = UUID.fromString(startJson.get("data").get("id").asText());

        // Submit quiz
        SubmitQuizRequest submitRequest = SubmitQuizRequest.builder()
                .answers(List.of(
                        SubmitAnswerRequest.builder().questionId(q1Id).selectedAnswer("A").build(),
                        SubmitAnswerRequest.builder().questionId(q2Id).selectedAnswer("C").build()
                ))
                .build();

        mockMvc.perform(post("/api/v1/submissions/" + submissionId + "/submit")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.data.score").value(50.0))
                .andExpect(jsonPath("$.data.percentage").value(100.0));

        // Database Verification: Check submission table
        Submission dbSubmission = submissionRepository.findById(submissionId).orElseThrow();
        assertThat(dbSubmission.getId()).isEqualTo(submissionId);
        assertThat(dbSubmission.getQuizId()).isEqualTo(quizId);
        assertThat(dbSubmission.getUserId()).isEqualTo(userId);
        assertThat(dbSubmission.getScore()).isEqualTo(50.0);
        assertThat(dbSubmission.getPercentage()).isEqualTo(100.0);
        assertThat(dbSubmission.getStatus()).isEqualTo(SubmissionStatus.SUBMITTED);
        assertThat(dbSubmission.getSubmittedAt()).isNotNull();

        // Database Verification: Check submission_answers table
        List<SubmissionAnswer> dbAnswers = submissionAnswerRepository.findBySubmissionId(submissionId);
        assertThat(dbAnswers).hasSize(2);

        SubmissionAnswer a1 = dbAnswers.stream().filter(a -> a.getQuestionId().equals(q1Id)).findFirst().orElseThrow();
        assertThat(a1.getSelectedAnswer()).isEqualTo("A");
        assertThat(a1.getCorrectAnswer()).isEqualTo("A");
        assertThat(a1.getIsCorrect()).isTrue();
        assertThat(a1.getMarksAwarded()).isEqualTo(20.0);

        SubmissionAnswer a2 = dbAnswers.stream().filter(a -> a.getQuestionId().equals(q2Id)).findFirst().orElseThrow();
        assertThat(a2.getSelectedAnswer()).isEqualTo("C");
        assertThat(a2.getCorrectAnswer()).isEqualTo("C");
        assertThat(a2.getIsCorrect()).isTrue();
        assertThat(a2.getMarksAwarded()).isEqualTo(30.0);
    }

    @Test
    @DisplayName("Test 4 — Wrong Answers (Score = 0, Percentage = 0)")
    void test4_WrongAnswers() throws Exception {
        InternalQuizResponse quiz = InternalQuizResponse.builder()
                .id(quizId).totalMarks(50).status("PUBLISHED").build();
        when(quizServiceClient.getQuiz(quizId)).thenReturn(quiz);

        UUID q1Id = UUID.randomUUID();
        UUID q2Id = UUID.randomUUID();
        InternalQuestionResponse q1 = InternalQuestionResponse.builder().id(q1Id).quizId(quizId).correctAnswer("A").marks(20).build();
        InternalQuestionResponse q2 = InternalQuestionResponse.builder().id(q2Id).quizId(quizId).correctAnswer("C").marks(30).build();
        when(questionServiceClient.getQuestions(quizId)).thenReturn(List.of(q1, q2));

        Submission submission = submissionRepository.save(Submission.builder()
                .quizId(quizId).userId(userId).totalMarks(50).status(SubmissionStatus.IN_PROGRESS).build());

        SubmitQuizRequest submitRequest = SubmitQuizRequest.builder()
                .answers(List.of(
                        SubmitAnswerRequest.builder().questionId(q1Id).selectedAnswer("B").build(), // Wrong
                        SubmitAnswerRequest.builder().questionId(q2Id).selectedAnswer("D").build()  // Wrong
                ))
                .build();

        mockMvc.perform(post("/api/v1/submissions/" + submission.getId() + "/submit")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.score").value(0.0))
                .andExpect(jsonPath("$.data.percentage").value(0.0))
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));
    }

    @Test
    @DisplayName("Test 5 — All Correct (Score = totalMarks, Percentage = 100)")
    void test5_AllCorrect() throws Exception {
        InternalQuizResponse quiz = InternalQuizResponse.builder()
                .id(quizId).totalMarks(40).status("PUBLISHED").build();
        when(quizServiceClient.getQuiz(quizId)).thenReturn(quiz);

        UUID q1Id = UUID.randomUUID();
        InternalQuestionResponse q1 = InternalQuestionResponse.builder().id(q1Id).quizId(quizId).correctAnswer("Java").marks(40).build();
        when(questionServiceClient.getQuestions(quizId)).thenReturn(List.of(q1));

        Submission submission = submissionRepository.save(Submission.builder()
                .quizId(quizId).userId(userId).totalMarks(40).status(SubmissionStatus.IN_PROGRESS).build());

        SubmitQuizRequest submitRequest = SubmitQuizRequest.builder()
                .answers(List.of(SubmitAnswerRequest.builder().questionId(q1Id).selectedAnswer("java").build())) // case-insensitive match
                .build();

        mockMvc.perform(post("/api/v1/submissions/" + submission.getId() + "/submit")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.score").value(40.0))
                .andExpect(jsonPath("$.data.percentage").value(100.0));
    }

    @Test
    @DisplayName("Test 6 — Mixed Answers (5 questions: 3 correct, 2 wrong)")
    void test6_MixedAnswers() throws Exception {
        InternalQuizResponse quiz = InternalQuizResponse.builder()
                .id(quizId).totalMarks(50).status("PUBLISHED").build();
        when(quizServiceClient.getQuiz(quizId)).thenReturn(quiz);

        UUID q1Id = UUID.randomUUID(), q2Id = UUID.randomUUID(), q3Id = UUID.randomUUID(), q4Id = UUID.randomUUID(), q5Id = UUID.randomUUID();

        InternalQuestionResponse q1 = InternalQuestionResponse.builder().id(q1Id).quizId(quizId).correctAnswer("A").marks(10).build();
        InternalQuestionResponse q2 = InternalQuestionResponse.builder().id(q2Id).quizId(quizId).correctAnswer("B").marks(10).build();
        InternalQuestionResponse q3 = InternalQuestionResponse.builder().id(q3Id).quizId(quizId).correctAnswer("C").marks(10).build();
        InternalQuestionResponse q4 = InternalQuestionResponse.builder().id(q4Id).quizId(quizId).correctAnswer("D").marks(10).build();
        InternalQuestionResponse q5 = InternalQuestionResponse.builder().id(q5Id).quizId(quizId).correctAnswer("A").marks(10).build();

        when(questionServiceClient.getQuestions(quizId)).thenReturn(List.of(q1, q2, q3, q4, q5));

        Submission submission = submissionRepository.save(Submission.builder()
                .quizId(quizId).userId(userId).totalMarks(50).status(SubmissionStatus.IN_PROGRESS).build());

        SubmitQuizRequest submitRequest = SubmitQuizRequest.builder()
                .answers(List.of(
                        SubmitAnswerRequest.builder().questionId(q1Id).selectedAnswer("A").build(), // Correct (10)
                        SubmitAnswerRequest.builder().questionId(q2Id).selectedAnswer("B").build(), // Correct (10)
                        SubmitAnswerRequest.builder().questionId(q3Id).selectedAnswer("C").build(), // Correct (10)
                        SubmitAnswerRequest.builder().questionId(q4Id).selectedAnswer("X").build(), // Wrong (0)
                        SubmitAnswerRequest.builder().questionId(q5Id).selectedAnswer("Y").build()  // Wrong (0)
                ))
                .build();

        mockMvc.perform(post("/api/v1/submissions/" + submission.getId() + "/submit")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.score").value(30.0))
                .andExpect(jsonPath("$.data.percentage").value(60.0));

        List<SubmissionAnswer> dbAnswers = submissionAnswerRepository.findBySubmissionId(submission.getId());
        assertThat(dbAnswers).hasSize(5);
        long correctCount = dbAnswers.stream().filter(a -> Boolean.TRUE.equals(a.getIsCorrect())).count();
        assertThat(correctCount).isEqualTo(3);
    }

    @Test
    @DisplayName("Test 7 — Invalid Quiz (404 Not Found from Feign Client)")
    void test7_InvalidQuiz() throws Exception {
        UUID invalidQuizId = UUID.randomUUID();
        Request request = Request.create(Request.HttpMethod.GET, "/api/v1/internal/quizzes/" + invalidQuizId,
                Collections.emptyMap(), null, java.nio.charset.StandardCharsets.UTF_8, null);
        when(quizServiceClient.getQuiz(invalidQuizId)).thenThrow(new FeignException.NotFound("Quiz not found", request, null, null));

        StartSubmissionRequest startRequest = StartSubmissionRequest.builder().quizId(invalidQuizId).build();

        mockMvc.perform(post("/api/v1/submissions/start")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Test 8 — Unpublished Quiz (Status DRAFT -> 400 Bad Request)")
    void test8_UnpublishedQuiz() throws Exception {
        InternalQuizResponse draftQuiz = InternalQuizResponse.builder()
                .id(quizId).totalMarks(50).status("DRAFT").build();
        when(quizServiceClient.getQuiz(quizId)).thenReturn(draftQuiz);

        StartSubmissionRequest startRequest = StartSubmissionRequest.builder().quizId(quizId).build();

        mockMvc.perform(post("/api/v1/submissions/start")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Quiz is not published."));
    }
}
