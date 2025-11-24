package io.cockroachdb.pest.workload.model;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;

public class Problem {
    public static Problem of(Throwable t) {
        return new Problem(t.getClass().getSimpleName(),
                t.getMessage(),
                toString(t));
    }

    private static String toString(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw, true));
        return sw.toString();
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
