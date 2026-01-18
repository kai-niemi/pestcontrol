package io.cockroachdb.pest.cluster;

import io.cockroachdb.pest.domain.Cluster;
import io.cockroachdb.pest.domain.ClusterType;

public interface ClusterOperator {
    boolean supports(ClusterType clusterType);

    StatusOperator statusOperator(Cluster cluster);

    DisruptionOperator disruptionOperator(Cluster cluster);

    ProxyOperator proxyOperator(Cluster cluster);

    NodeOperator nodeOperator(Cluster cluster);
}
