package io.cockroachdb.pest.model;

import java.util.EnumSet;

public abstract class ClusterTypes {
    private ClusterTypes() {
    }

    public static boolean isCloud(ClusterType clusterType) {
        return EnumSet.of(
                        ClusterType.cloud_dedicated,
                        ClusterType.cloud_serverless,
                        ClusterType.cloud_standard)
                .contains(clusterType);
    }

    public static boolean isHosted(ClusterType clusterType) {
        return EnumSet.of(
                        ClusterType.hosted_insecure,
                        ClusterType.hosted_secure)
                .contains(clusterType);
    }

    public static boolean isLocal(ClusterType clusterType) {
        return EnumSet.of(
                        ClusterType.local_insecure,
                        ClusterType.local_secure)
                .contains(clusterType);
    }

    public static boolean isSecure(ClusterType clusterType) {
        return EnumSet.of(
                        ClusterType.hosted_secure,
                        ClusterType.local_secure)
                .contains(clusterType);
    }
}
