package io.cockroachdb.pest.cluster;

public abstract class ClusterException extends RuntimeException {
    public ClusterException(String message) {
        super(message);
    }

    public ClusterException(String message, Throwable cause) {
        super(message, cause);
    }
}
