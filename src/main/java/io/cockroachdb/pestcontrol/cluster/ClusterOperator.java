package io.cockroachdb.pestcontrol.cluster;

import io.cockroachdb.pestcontrol.model.ClusterProperties;
import io.cockroachdb.pestcontrol.model.ClusterType;

public interface ClusterOperator {
    boolean supports(ClusterType clusterType);

    String install(ClusterProperties cluster, Integer nodeId);

    String init(ClusterProperties cluster, Integer nodeId);

    String startNode(ClusterProperties cluster, Integer nodeId);

    String stopNode(ClusterProperties cluster, Integer nodeId);

    String killNode(ClusterProperties cluster, Integer nodeId);

    String disruptNode(ClusterProperties cluster, Integer nodeId);

    String recoverNode(ClusterProperties cluster, Integer nodeId);

    String disruptNodes(ClusterProperties cluster, String locality);

    String recoverNodes(ClusterProperties cluster, String locality);
}
