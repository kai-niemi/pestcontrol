package io.cockroachdb.pest.cluster;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import io.cockroachdb.pest.model.ClusterProperties;
import io.cockroachdb.pest.model.ClusterType;

public interface ClusterOperator {
    boolean supports(ClusterType clusterType);

    String certs(ClusterProperties clusterProperties, List<Integer> nodeIds, Map<Integer, List<Path>> keyFiles);

    String install(ClusterProperties cluster, Integer nodeId);

    String init(ClusterProperties cluster, Integer nodeId);

    String wipe(ClusterProperties cluster, Integer nodeId);

    String startNode(ClusterProperties cluster, Integer nodeId);

    String stopNode(ClusterProperties cluster, Integer nodeId);

    String killNode(ClusterProperties cluster, Integer nodeId);

    String sqlNode(ClusterProperties cluster, Integer nodeId);

    String disruptNode(ClusterProperties cluster, Integer nodeId);

    String recoverNode(ClusterProperties cluster, Integer nodeId);

    String disruptNodes(ClusterProperties cluster, String locality);

    String recoverNodes(ClusterProperties cluster, String locality);

    String startProxyServer(ClusterProperties cluster);

    String stopProxyServer(ClusterProperties cluster);

    String startProxyClient(ClusterProperties cluster, Integer nodeId);

    String startLoadBalancer(ClusterProperties cluster, Integer nodeId);

    String stopLoadBalancer(ClusterProperties cluster);
}
