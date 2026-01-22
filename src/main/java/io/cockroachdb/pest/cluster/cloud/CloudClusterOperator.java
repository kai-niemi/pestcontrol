package io.cockroachdb.pest.cluster.cloud;

import java.util.EnumSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.cluster.DisruptionOperator;
import io.cockroachdb.pest.cluster.NodeOperator;
import io.cockroachdb.pest.cluster.ProxyOperator;
import io.cockroachdb.pest.cluster.StatusOperator;
import io.cockroachdb.pest.cluster.local.MetaDataRepository;
import io.cockroachdb.pest.config.RestClientProvider;
import io.cockroachdb.pest.model.ApplicationProperties;
import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.model.ClusterType;

@Component
public class CloudClusterOperator implements ClusterOperator {
    @Autowired
    private RestClientProvider restClientProvider;

    @Autowired
    private MetaDataRepository metaDataRepository;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Override
    public StatusOperator statusOperator(Cluster cluster) {
        RestClient restClient = restClientProvider.apply(cluster.getClusterType());
        return new CloudStatusOperator(cluster, restClient, metaDataRepository, applicationProperties);
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
