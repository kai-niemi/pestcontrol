package io.cockroachdb.pest.cluster;

import java.util.EnumSet;
import java.util.List;

import io.cockroachdb.pest.model.ClusterProperties;
import io.cockroachdb.pest.api.cluster.NodeModel;
import io.cockroachdb.pest.model.ClusterType;
import io.cockroachdb.pest.cluster.schema.NodeDetail;
import io.cockroachdb.pest.cluster.schema.NodeStatus;

public interface ClusterManager {
    List<String> getClusterIds();

    String getClusterVersion(String clusterId);

    void setCredentialsHandler(CredentialsHandler credentialsHandler);

    String login(String clusterId, String userName, String password);

    boolean logout(String clusterId);

    boolean hasSessionToken(String clusterId);

    NodeDetail queryNodeDetailById(String clusterId, Integer id);

    NodeStatus queryNodeStatusById(String clusterId, Integer id);

    NodeModel queryNodeById(String clusterId, Integer id);

    List<NodeModel> queryAllNodes(String clusterId);

    ClusterProperties getClusterProperties(String clusterId);

    ClusterProperties getClusterProperties(String clusterId, EnumSet<ClusterType> clusterTypes);

    ClusterOperator getClusterOperator(String clusterId);
}
