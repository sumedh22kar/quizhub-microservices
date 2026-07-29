package com.quizhub.submissionservice.kafka;

import com.quizhub.submissionservice.event.QuizSubmittedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuizSubmittedProducer {

    private final KafkaTemplate<String, QuizSubmittedEvent> kafkaTemplate;

    public void sendQuizSubmittedEvent(QuizSubmittedEvent event) {
        log.info("Publishing QuizSubmittedEvent for submissionId: {}", event.getSubmissionId());
        kafkaTemplate.send("quiz-submitted", event);
    }
}
