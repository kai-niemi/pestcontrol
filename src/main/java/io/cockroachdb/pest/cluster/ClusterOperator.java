package io.cockroachdb.pest.cluster;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import io.cockroachdb.pest.model.ClusterSettings;
import io.cockroachdb.pest.model.ClusterType;

public interface ClusterOperator {
    boolean supports(ClusterType clusterType);

    String certs(ClusterSettings clusterSettings, List<Integer> nodeIds, Map<Integer, List<Path>> keyFiles);

    String install(ClusterSettings cluster, Integer nodeId);

    String init(ClusterSettings cluster, Integer nodeId);

    String wipe(ClusterSettings cluster, Integer nodeId);

    String startNode(ClusterSettings cluster, Integer nodeId);

    String stopNode(ClusterSettings cluster, Integer nodeId);

    String killNode(ClusterSettings cluster, Integer nodeId);

    String sqlNode(ClusterSettings cluster, Integer nodeId);

    String disruptNode(ClusterSettings cluster, Integer nodeId);

    String recoverNode(ClusterSettings cluster, Integer nodeId);

    String disruptLocality(ClusterSettings cluster, String locality);

    String recoverLocality(ClusterSettings cluster, String locality);

    String startProxyServer(ClusterSettings cluster);

    String stopProxyServer(ClusterSettings cluster);

    String startProxyClient(ClusterSettings cluster, Integer nodeId);

    String startLoadBalancer(ClusterSettings cluster, Integer nodeId);

    String stopLoadBalancer(ClusterSettings cluster);
}
