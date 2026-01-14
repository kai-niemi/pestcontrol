package io.cockroachdb.pest.util;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class DataPoint<K, V> {
    private final Instant instant;

    private final Map<K, V> metrics = new LinkedHashMap<>();

    public DataPoint(Instant instant) {
        this.instant = instant;
    }

    public boolean isExpired() {
        return false;
    }

    public Instant getInstant() {
        return instant;
    }

    public void putValue(K id, V metric) {
        metrics.put(id, metric);
    }

    public V getValue(K id, V defaultValue) {
        return metrics.getOrDefault(id, defaultValue);
    }
}
