package io.cockroachdb.pestcontrol.operator;

import io.cockroachdb.pestcontrol.model.ClusterProperties;
import io.cockroachdb.pestcontrol.model.ClusterType;

public interface ClusterOperator {
    boolean supports(ClusterType clusterType);

    void install(ClusterProperties cluster, Integer nodeId);

    void init(ClusterProperties cluster, Integer nodeId);

    void startNode(ClusterProperties cluster, Integer nodeId);

    void stopNode(ClusterProperties cluster, Integer nodeId);

    void killNode(ClusterProperties cluster, Integer nodeId);

    void disruptNode(ClusterProperties cluster, Integer nodeId);

    void recoverNode(ClusterProperties cluster, Integer nodeId);

    void disruptNodes(ClusterProperties cluster, String locality);

    void recoverNodes(ClusterProperties cluster, String locality);
}
