package io.cockroachdb.pest.cluster.cloud;

import java.util.EnumSet;

import org.springframework.stereotype.Component;

import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.cluster.DisruptionOperator;
import io.cockroachdb.pest.cluster.StatusOperator;
import io.cockroachdb.pest.cluster.NodeOperator;
import io.cockroachdb.pest.cluster.ProxyOperator;
import io.cockroachdb.pest.domain.Cluster;
import io.cockroachdb.pest.domain.ClusterType;

@Component
public class CloudClusterOperator implements ClusterOperator {
    @Override
    public StatusOperator statusOperator(Cluster cluster) {
        return new CloudStatusOperator();
    }

    @Override
    public DisruptionOperator disruptionOperator(Cluster cluster) {
        return new CloudDisruptionOperator(cluster);
    }

    @Override
    public NodeOperator nodeOperator(Cluster cluster) {
        throw new UnsupportedOperationException("Cloud cluster does not support node operator");
    }

    @Override
    public ProxyOperator proxyOperator(Cluster cluster) {
        throw new UnsupportedOperationException("Cloud cluster does not support proxy operator");
    }

    @Override
    public boolean supports(ClusterType clusterType) {
        return EnumSet.of(ClusterType.cloud_dedicated).contains(clusterType);
    }
}
