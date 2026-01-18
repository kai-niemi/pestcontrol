package io.cockroachdb.pest.cluster.hosted;

import java.io.IOException;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import io.cockroachdb.pest.cluster.DisruptionOperator;
import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.cluster.StatusOperator;
import io.cockroachdb.pest.cluster.NodeOperator;
import io.cockroachdb.pest.cluster.ProxyOperator;
import io.cockroachdb.pest.cluster.repository.MetaDataRepository;
import io.cockroachdb.pest.cluster.local.LocalStatusOperator;
import io.cockroachdb.pest.config.RestClientProvider;
import io.cockroachdb.pest.domain.Cluster;
import io.cockroachdb.pest.domain.ClusterType;
import io.cockroachdb.pest.util.HypermediaClient;

@Component
public class HostedClusterOperator implements ClusterOperator {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private HypermediaClient hypermediaClient;

    @Autowired
    private RestClientProvider restClientProvider;

    @Autowired
    private MetaDataRepository metaDataRepository;

    @Autowired
    @Qualifier("localClusterOperator")
    private ClusterOperator localClusterOperator;

    @Override
    public StatusOperator statusOperator(Cluster cluster) {
        RestClient restClient = restClientProvider.apply(cluster.getClusterType());
        return new LocalStatusOperator(cluster, restClient, metaDataRepository);
    }

    @Override
    public DisruptionOperator disruptionOperator(Cluster cluster) {
        return new DisruptionOperator() {
            @Override
            public String disruptLocality(String locality) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String recoverLocality(String locality) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String disruptNode(Integer nodeId) throws IOException {
                return nodeOperator(cluster).killNode(nodeId);
            }

            @Override
            public String recoverNode(Integer nodeId)  throws IOException {
                return nodeOperator(cluster).startNode(nodeId);
            }
        };
    }

    @Override
    public NodeOperator nodeOperator(Cluster cluster) {
        return new HostedNodeOperator(cluster, hypermediaClient, localClusterOperator.nodeOperator(cluster));
    }

    @Override
    public ProxyOperator proxyOperator(Cluster cluster) {
        return new HostedProxyOperator(cluster, hypermediaClient);
    }

    @Override
    public boolean supports(ClusterType clusterType) {
        return EnumSet.of(ClusterType.hosted_insecure, ClusterType.hosted_secure)
                .contains(clusterType);
    }
}
