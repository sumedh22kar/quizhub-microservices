package com.quizhub.aiagent.config;

import com.quizhub.aiagent.metrics.AiMetrics;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MeteredCacheManager implements CacheManager {

    private final CacheManager delegate;
    private final AiMetrics aiMetrics;

    private final Map<String, Cache> cacheMap =
            new ConcurrentHashMap<>();

    public MeteredCacheManager(
            CacheManager delegate,
            AiMetrics aiMetrics
    ) {
        this.delegate = delegate;
        this.aiMetrics = aiMetrics;
    }

    @Override
    public Cache getCache(String name) {

        return cacheMap.computeIfAbsent(
                name,
                cacheName -> {

                    Cache cache =
                            delegate.getCache(cacheName);

                    if (cache == null) {
                        return null;
                    }

                    return new MeteredCache(
                            cache,
                            aiMetrics
                    );
                }
        );
    }

    @Override
    public Collection<String> getCacheNames() {
        return delegate.getCacheNames();
    }
}
