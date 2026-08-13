package com.quizhub.aiagent.config;

import com.quizhub.aiagent.metrics.AiMetrics;
import org.springframework.cache.Cache;

import java.util.concurrent.Callable;

public class MeteredCache implements Cache {

    private final Cache delegate;
    private final AiMetrics aiMetrics;

    public MeteredCache(
            Cache delegate,
            AiMetrics aiMetrics
    ) {
        this.delegate = delegate;
        this.aiMetrics = aiMetrics;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Object getNativeCache() {
        return delegate.getNativeCache();
    }

    @Override
    public ValueWrapper get(Object key) {

        ValueWrapper value = delegate.get(key);

        if (value != null) {
            aiMetrics.cacheHits().increment();
        } else {
            aiMetrics.cacheMisses().increment();
        }

        return value;
    }

    @Override
    public <T> T get(
            Object key,
            Class<T> type
    ) {

        T value = delegate.get(key, type);

        if (value != null) {
            aiMetrics.cacheHits().increment();
        } else {
            aiMetrics.cacheMisses().increment();
        }

        return value;
    }

    @Override
    public <T> T get(
            Object key,
            Callable<T> valueLoader
    ) {

        try {
            return delegate.get(key, valueLoader);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void put(
            Object key,
            Object value
    ) {
        delegate.put(key, value);
    }

    @Override
    public void evict(Object key) {
        delegate.evict(key);
    }

    @Override
    public void clear() {
        delegate.clear();
    }
}
