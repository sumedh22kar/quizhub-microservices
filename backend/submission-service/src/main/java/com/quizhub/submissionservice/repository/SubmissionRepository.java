package com.quizhub.submissionservice.repository;

import com.quizhub.submissionservice.entity.Submission;
import com.quizhub.submissionservice.entity.enums.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    List<Submission> findByUserId(UUID userId);

    List<Submission> findByQuizId(UUID quizId);

    Optional<Submission> findByIdAndUserId(UUID id, UUID userId);

    Optional<Submission> findByUserIdAndQuizIdAndStatus(UUID userId, UUID quizId, SubmissionStatus status);
}
