package com.quizhub.submissionservice.repository;

import com.quizhub.submissionservice.entity.SubmissionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubmissionAnswerRepository extends JpaRepository<SubmissionAnswer, UUID> {

    List<SubmissionAnswer> findBySubmissionId(UUID submissionId);
}
