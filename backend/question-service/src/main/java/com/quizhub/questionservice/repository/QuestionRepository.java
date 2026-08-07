package com.quizhub.questionservice.repository;

import com.quizhub.questionservice.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {

    List<Question> findByQuizId(UUID quizId);

    List<Question> findByQuizIdAndActiveTrue(UUID quizId);

    long countByQuizId(UUID quizId);

    List<Question> findByIdIn(List<UUID> ids);

}
