package io.cockroachdb.pc.workload.model;

import java.time.Instant;

import io.cockroachdb.pc.util.ExceptionUtils;

public class Problem {
    public static Problem of(Throwable t) {
        return new Problem(t.getClass().getSimpleName(),
                t.getMessage(),
                ExceptionUtils.toString(t));
    }

    private final String className;

    private final String message;

    private final String stackTrace;

    private final Instant createdAt = Instant.now();

    private boolean isTransient;

    public Problem(String className, String message, String stackTrace) {
        this.className = className;
        this.message = message;
        this.stackTrace = stackTrace;
    }

    public boolean isTransient() {
        return isTransient;
    }

    public Problem setTransient(boolean aTransient) {
        isTransient = aTransient;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getClassName() {
        return className;
    }

    public String getMessage() {
        return message;
    }

    public String getStackTrace() {
        return stackTrace;
    }
}
