package io.cockroachdb.pest.cluster;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;

import io.cockroachdb.pest.domain.ApplicationProperties;
import io.cockroachdb.pest.domain.Cluster;
import io.cockroachdb.pest.domain.ClusterType;

@Component
public class ClusterOperatorProvider {
    @Autowired
    private ObjectProvider<ClusterOperator> clusterOperators;

    @Autowired
    private ApplicationProperties applicationProperties;

    public Cluster clusterById(String clusterId) {
        return applicationProperties.getClusterById(clusterId);
    }

    public ClusterOperator clusterOperator(String clusterId) {
        ClusterType clusterType = applicationProperties.getClusterById(clusterId).getClusterType();
        return clusterOperators
                .stream()
                .filter(x -> x.supports(clusterType))
                .min(new AnnotationAwareOrderComparator())
                .orElseThrow(() -> new UnsupportedOperationException(
                        "No operator found for cluster type: " + clusterType));
    }
}
