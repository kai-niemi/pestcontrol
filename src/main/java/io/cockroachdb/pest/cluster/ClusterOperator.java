package io.cockroachdb.pest.cluster;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import io.cockroachdb.pest.model.ClusterProperties;
import io.cockroachdb.pest.model.ClusterType;

public interface ClusterOperator {
    boolean supports(ClusterType clusterType);

    Map<Integer, List<Path>> certs(ClusterProperties clusterProperties, List<Integer> nodeIds);

    String install(ClusterProperties cluster, Integer nodeId);

    String init(ClusterProperties cluster, Integer nodeId);

    String startNode(ClusterProperties cluster, Integer nodeId);

    String stopNode(ClusterProperties cluster, Integer nodeId);

    String killNode(ClusterProperties cluster, Integer nodeId);

    String sqlNode(ClusterProperties cluster, Integer nodeId);

    String disruptNode(ClusterProperties cluster, Integer nodeId);

    String recoverNode(ClusterProperties cluster, Integer nodeId);

    String disruptNodes(ClusterProperties cluster, String locality);

    String recoverNodes(ClusterProperties cluster, String locality);
}
