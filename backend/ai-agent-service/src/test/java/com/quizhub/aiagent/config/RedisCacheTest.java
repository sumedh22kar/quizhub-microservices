package com.quizhub.aiagent.config;

import com.quizhub.aiagent.dto.response.AIResponse;
import com.quizhub.aiagent.dto.response.QuestionAnalysisResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheManager;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RedisCacheTest {

    @Autowired
    private RedisCacheManager cacheManager;

    @Test
    void testCachePutAndGetForString() {
        Cache cache = cacheManager.getCache("ai-study-plan");
        assertNotNull(cache);

        UUID submissionId = UUID.randomUUID();
        String studyPlan = "Week 1: Java Basics\nWeek 2: Advanced OOP";

        cache.put(submissionId, studyPlan);

        Cache.ValueWrapper wrapper = cache.get(submissionId);
        assertNotNull(wrapper, "Cache value should not be null");
        assertEquals(studyPlan, wrapper.get());
    }

    @Test
    void testCachePutAndGetForAIResponse() {
        Cache cache = cacheManager.getCache("ai-explain");
        assertNotNull(cache);

        UUID questionId = UUID.randomUUID();
        AIResponse response = AIResponse.builder()
                .answer("Option A is correct because...")
                .model("qwen3:8b")
                .responseTime(1500L)
                .build();

        cache.put(questionId, response);

        Cache.ValueWrapper wrapper = cache.get(questionId);
        assertNotNull(wrapper, "Cache value should not be null");
        AIResponse cached = (AIResponse) wrapper.get();
        assertEquals("qwen3:8b", cached.getModel());
        assertEquals("Option A is correct because...", cached.getAnswer());
    }

    @Test
    void testCachePutAndGetForQuestionAnalysisResponse() {
        Cache cache = cacheManager.getCache("ai-analysis");
        assertNotNull(cache);

        UUID questionId = UUID.randomUUID();
        QuestionAnalysisResponse analysis = QuestionAnalysisResponse.builder()
                .difficulty("MEDIUM")
                .estimatedTime("2 minutes")
                .concepts(List.of("Polymorphism", "Inheritance"))
                .commonMistakes(List.of("Confusing overloading and overriding"))
                .recommendedTopics(List.of("OOP"))
                .build();

        cache.put(questionId, analysis);

        Cache.ValueWrapper wrapper = cache.get(questionId);
        assertNotNull(wrapper, "Cache value should not be null");
        QuestionAnalysisResponse cached = (QuestionAnalysisResponse) wrapper.get();
        assertEquals("MEDIUM", cached.getDifficulty());
        assertEquals(2, cached.getConcepts().size());
    }
}
