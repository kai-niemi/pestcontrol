package io.cockroachdb.pc.service;

import io.cockroachdb.pc.model.ClusterProperties;
import io.cockroachdb.pc.schema.ClusterType;
import io.cockroachdb.pc.schema.NodeModel;
import io.cockroachdb.pc.schema.nodes.Locality;

public interface DisruptionManager {
    boolean supports(ClusterType clusterType);

    void disruptNode(ClusterProperties clusterProperties, NodeModel nodeModel);

    void recoverNode(ClusterProperties clusterProperties, NodeModel nodeModel);

    void disruptLocality(ClusterProperties clusterProperties, Locality locality);

    void recoverLocality(ClusterProperties clusterProperties, Locality locality);
}
