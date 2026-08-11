package com.quizhub.aiagent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private static final int MAX_REQUESTS = 10;
    private static final int WINDOW_SECONDS = 60;

    private final RedisTemplate<String, String> redisTemplate;

    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT =
            new DefaultRedisScript<>(
                    """
                    local current = redis.call('INCR', KEYS[1])

                    if current == 1 then
                        redis.call('EXPIRE', KEYS[1], ARGV[1])
                    end

                    return current
                    """,
                    Long.class
            );

    public boolean isAllowed(String clientId) {

        String key = "ai-rate-limit:" + clientId;

        Long count = redisTemplate.execute(
                RATE_LIMIT_SCRIPT,
                Collections.singletonList(key),
                String.valueOf(WINDOW_SECONDS)
        );

        return count != null && count <= MAX_REQUESTS;
    }
}