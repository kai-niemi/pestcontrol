package io.cockroachdb.pest.workload.model;

public enum WorkloadStatus {
    running("text-bg-info"),
    completed("text-bg-success"),
    cancelled("text-bg-warning"),
    failed("text-bg-danger");

    final String badge;

    WorkloadStatus(String badge) {
        this.badge = badge;
    }

    public String getBadge() {
        return badge;
    }
}
