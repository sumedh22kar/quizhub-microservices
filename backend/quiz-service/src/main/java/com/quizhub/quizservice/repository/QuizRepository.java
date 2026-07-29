package com.quizhub.quizservice.repository;

import com.quizhub.quizservice.entity.Quiz;
import com.quizhub.quizservice.entity.enums.QuizStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, UUID> {

    List<Quiz> findAllByOwnerId(UUID ownerId);

    List<Quiz> findAllByStatus(QuizStatus status);

    List<Quiz> findAllByOwnerIdAndStatus(UUID ownerId, QuizStatus status);
}
