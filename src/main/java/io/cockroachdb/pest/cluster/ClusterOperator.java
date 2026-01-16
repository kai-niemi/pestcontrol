package io.cockroachdb.pest.cluster;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import io.cockroachdb.pest.domain.Cluster;
import io.cockroachdb.pest.domain.ClusterType;

public interface ClusterOperator {
    boolean supports(ClusterType clusterType);

    String certs(Cluster cluster, List<Integer> nodeIds, Map<Integer, List<Path>> keyFiles);

    String install(Cluster cluster, Integer nodeId);

    String init(Cluster cluster, Integer nodeId);

    String wipe(Cluster cluster, Integer nodeId, boolean all);

    String startNode(Cluster cluster, Integer nodeId);

    String stopNode(Cluster cluster, Integer nodeId);

    String killNode(Cluster cluster, Integer nodeId);

    String sqlNode(Cluster cluster, Integer nodeId);

    String statusNode(Cluster cluster, Integer nodeId);

    String disruptNode(Cluster cluster, Integer nodeId);

    String recoverNode(Cluster cluster, Integer nodeId);

    String disruptLocality(Cluster cluster, String locality);

    String recoverLocality(Cluster cluster, String locality);

    String startToxiproxyServer();

    String stopToxiproxyServer();

    String genHAProxyCfg(Cluster cluster, Integer nodeId);

    String startHAProxy(Cluster cluster, Integer nodeId);

    String stopHAProxy(Cluster cluster, Integer nodeId);
}
