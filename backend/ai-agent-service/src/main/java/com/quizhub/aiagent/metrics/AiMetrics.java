package com.quizhub.aiagent.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class AiMetrics {

    private final Counter llmRequests;

    private final Counter llmFailures;

    private final Counter llmRetries;

    private final Counter cacheHits;

    private final Counter cacheMisses;

    private final Counter rateLimitExceeded;

    private final Timer llmDuration;

    public AiMetrics(MeterRegistry meterRegistry) {

        llmRequests = Counter.builder("quizhub_ai_llm_requests_total")
                .description("Total number of LLM requests")
                .register(meterRegistry);

        llmFailures = Counter.builder("quizhub_ai_llm_failures_total")
                .description("Total number of failed LLM requests")
                .register(meterRegistry);

        llmRetries = Counter.builder("quizhub_ai_llm_retries_total")
                .description("Total number of LLM retry attempts")
                .register(meterRegistry);

        cacheHits = Counter.builder("quizhub_ai_cache_hits_total")
                .description("Total number of AI cache hits")
                .register(meterRegistry);

        cacheMisses = Counter.builder("quizhub_ai_cache_misses_total")
                .description("Total number of AI cache misses")
                .register(meterRegistry);

        rateLimitExceeded = Counter.builder("quizhub_ai_rate_limit_exceeded_total")
                .description("Total number of AI requests rejected by rate limiting")
                .register(meterRegistry);

        llmDuration = Timer.builder("quizhub_ai_llm_duration_seconds")
                .description("LLM request duration")
                .register(meterRegistry);
    }

    public Counter llmRequests() {
        return llmRequests;
    }

    public Counter llmFailures() {
        return llmFailures;
    }

    public Counter llmRetries() {
        return llmRetries;
    }

    public Counter cacheHits() {
        return cacheHits;
    }

    public Counter cacheMisses() {
        return cacheMisses;
    }

    public Counter rateLimitExceeded() {
        return rateLimitExceeded;
    }

    public Timer llmDuration() {
        return llmDuration;
    }
}