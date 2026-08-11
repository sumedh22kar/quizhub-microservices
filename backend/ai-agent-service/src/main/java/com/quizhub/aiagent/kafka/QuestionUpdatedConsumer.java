package com.quizhub.aiagent.kafka;

import com.quizhub.aiagent.event.QuestionUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuestionUpdatedConsumer {

    private final CacheManager cacheManager;

    @KafkaListener(
            topics = "question-updated",
            groupId = "ai-agent-cache-group"
    )
    public void consume(QuestionUpdatedEvent event) {

        UUID questionId = event.getQuestionId();

        log.info(
                "Received QuestionUpdatedEvent for questionId: {}",
                questionId
        );

        evict("ai-explain", questionId);
        evict("ai-hint", questionId);
        evict("ai-analysis", questionId);

        log.info(
                "AI cache invalidated for questionId: {}",
                questionId
        );
    }

    private void evict(String cacheName, Object key) {

        Cache cache = cacheManager.getCache(cacheName);

        if (cache != null) {
            cache.evict(key);
            if (key instanceof UUID) {
                cache.evict(key.toString());
            }

            log.info(
                    "Evicted cache={} key={}",
                    cacheName,
                    key
            );
        }
    }
}