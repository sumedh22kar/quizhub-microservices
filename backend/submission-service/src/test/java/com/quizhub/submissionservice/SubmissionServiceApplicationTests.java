package com.quizhub.submissionservice;

import com.quizhub.submissionservice.entity.Submission;
import com.quizhub.submissionservice.entity.SubmissionAnswer;
import com.quizhub.submissionservice.entity.enums.SubmissionStatus;
import com.quizhub.submissionservice.repository.SubmissionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SubmissionServiceApplicationTests {

	@Autowired
	private SubmissionRepository submissionRepository;

	@Test
	void contextLoads() {
		assertThat(submissionRepository).isNotNull();
	}

	@Test
	@DisplayName("Should save and retrieve a submission with answers")
	void shouldSaveAndRetrieveSubmissionWithAnswers() {
		UUID quizId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		UUID questionId = UUID.randomUUID();

		Submission submission = Submission.builder()
				.quizId(quizId)
				.userId(userId)
				.score(10.0)
				.totalMarks(10)
				.percentage(100.0)
				.status(SubmissionStatus.SUBMITTED)
				.startedAt(Instant.now().minusSeconds(300))
				.submittedAt(Instant.now())
				.build();

		SubmissionAnswer answer = SubmissionAnswer.builder()
				.questionId(questionId)
				.selectedAnswer("A")
				.correctAnswer("A")
				.isCorrect(true)
				.marksAwarded(10.0)
				.build();

		submission.addAnswer(answer);

		Submission savedSubmission = submissionRepository.save(submission);

		assertThat(savedSubmission.getId()).isNotNull();
		assertThat(savedSubmission.getAnswers()).hasSize(1);
		assertThat(savedSubmission.getAnswers().get(0).getId()).isNotNull();
		assertThat(savedSubmission.getAnswers().get(0).getSubmission()).isEqualTo(savedSubmission);
	}
}
