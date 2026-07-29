package com.quizhub.resultservice.repository;

import com.quizhub.resultservice.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResultRepository extends JpaRepository<Result, UUID> {

    Optional<Result> findBySubmissionId(UUID submissionId);

    List<Result> findByUserId(UUID userId);

    List<Result> findByQuizId(UUID quizId);

    List<Result> findByQuizIdOrderByScoreDesc(UUID quizId);
}
