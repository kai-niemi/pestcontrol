package io.cockroachdb.pest.cluster;

import java.util.List;

import io.cockroachdb.pest.cluster.model.NodeDetail;
import io.cockroachdb.pest.cluster.model.NodeModel;
import io.cockroachdb.pest.cluster.model.NodeStatus;
import io.cockroachdb.pest.domain.Cluster;

public interface ClusterManager {
    Cluster getCluster(String clusterId);

    List<String> getClusterIds();

    String getClusterVersion(String clusterId);

    String login(String clusterId, String userName, String password);

    boolean logout(String clusterId);

    boolean hasSessionToken(String clusterId);

    void setCredentialsHandler(CredentialsHandler credentialsHandler);

    NodeDetail queryNodeDetailById(String clusterId, Integer id);

    NodeStatus queryNodeStatusById(String clusterId, Integer id);

    NodeModel queryNodeById(String clusterId, Integer id);

    List<NodeModel> queryAllNodes(String clusterId);
}
