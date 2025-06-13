package io.cockroachdb.pest.model;

public enum ClusterType {
    cloud_serverless,
    cloud_standard,
    cloud_dedicated,
    local_insecure,
    local_secure,
    remote_insecure,
    remote_secure,
    hosted_insecure,
    hosted_secure,
}
