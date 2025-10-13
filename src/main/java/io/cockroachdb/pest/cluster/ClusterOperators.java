package io.cockroachdb.pest.cluster;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;

import io.cockroachdb.pest.model.ClusterType;

@Component
public class ClusterOperators {
    @Autowired
    private ObjectProvider<ClusterOperator> clusterOperators;

    public ClusterOperator getClusterOperator(ClusterType clusterType) {
        return clusterOperators
                .stream()
                .filter(x -> x.supports(clusterType))
                .min(new AnnotationAwareOrderComparator())
                .orElseThrow(() -> new UnsupportedOperationException(
                        "No operator found for cluster type: " + clusterType));
    }
}
