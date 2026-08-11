package com.quizhub.aiagent.kafka;

import com.quizhub.aiagent.event.QuestionUpdatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuestionUpdatedConsumerTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache explainCache;

    @Mock
    private Cache hintCache;

    @Mock
    private Cache analysisCache;

    @InjectMocks
    private QuestionUpdatedConsumer consumer;

    @Test
    void testConsumeEvictsAllRelatedCaches() {
        UUID questionId = UUID.randomUUID();
        QuestionUpdatedEvent event = QuestionUpdatedEvent.builder()
                .questionId(questionId)
                .build();

        when(cacheManager.getCache("ai-explain")).thenReturn(explainCache);
        when(cacheManager.getCache("ai-hint")).thenReturn(hintCache);
        when(cacheManager.getCache("ai-analysis")).thenReturn(analysisCache);

        consumer.consume(event);

        verify(explainCache).evict(questionId);
        verify(hintCache).evict(questionId);
        verify(analysisCache).evict(questionId);
    }
}
