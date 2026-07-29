package com.quizhub.resultservice.kafka;

import com.quizhub.resultservice.event.QuizSubmittedEvent;
import com.quizhub.resultservice.service.ResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuizSubmittedConsumer {

    private final ResultService resultService;

    @KafkaListener(
            topics = "quiz-submitted",
            groupId = "result-service-group"
    )
    public void consume(QuizSubmittedEvent event) {
        log.info("Received QuizSubmittedEvent: {}", event);
        resultService.generateResult(event);
    }
}