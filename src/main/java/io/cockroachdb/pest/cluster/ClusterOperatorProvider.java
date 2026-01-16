package io.cockroachdb.pest.cluster;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;

import io.cockroachdb.pest.domain.Cluster;
import io.cockroachdb.pest.domain.ClusterType;

@Component
public class ClusterOperatorProvider {
    @Autowired
    private ObjectProvider<ClusterOperator> clusterOperators;

    public ClusterOperator clusterOperator(Cluster cluster) {
        return clusterOperator(cluster.getClusterType());
    }

    public ClusterOperator clusterOperator(ClusterType clusterType) {
        return clusterOperators
                .stream()
                .filter(x -> x.supports(clusterType))
                .min(new AnnotationAwareOrderComparator())
                .orElseThrow(() -> new UnsupportedOperationException(
                        "No cluster operator found for cluster type: " + clusterType));
    }
}
