package com.quizhub.questionservice.kafka;

import com.quizhub.questionservice.event.QuestionUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuestionUpdatedProducer {

    private static final String TOPIC = "question-updated";

    private final KafkaTemplate<String, QuestionUpdatedEvent> kafkaTemplate;

    public void sendQuestionUpdatedEvent(UUID questionId) {

        QuestionUpdatedEvent event =
                QuestionUpdatedEvent.builder()
                        .questionId(questionId)
                        .build();

        log.info(
                "Publishing QuestionUpdatedEvent for questionId: {}",
                questionId
        );

        kafkaTemplate.send(
                TOPIC,
                questionId.toString(),
                event
        );
    }
}