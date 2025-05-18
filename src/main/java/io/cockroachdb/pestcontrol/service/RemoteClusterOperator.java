package io.cockroachdb.pestcontrol.service;

import java.util.EnumSet;

import org.springframework.stereotype.Component;

import io.cockroachdb.pestcontrol.model.ClusterProperties;
import io.cockroachdb.pestcontrol.schema.ClusterType;
import io.cockroachdb.pestcontrol.schema.NodeModel;
import io.cockroachdb.pestcontrol.schema.nodes.Locality;

@Component
public class RemoteClusterOperator implements ClusterOperator {
    @Override
    public boolean supports(ClusterType clusterType) {
        return EnumSet.of(ClusterType.remote_insecure, ClusterType.remote_secure).contains(clusterType);
    }

    @Override
    public void disruptNode(ClusterProperties clusterProperties, NodeModel nodeModel) {

    }

    @Override
    public void recoverNode(ClusterProperties clusterProperties, NodeModel nodeModel) {

    }

    @Override
    public void disruptLocality(ClusterProperties clusterProperties, Locality locality) {

    }

    @Override
    public void recoverLocality(ClusterProperties clusterProperties, Locality locality) {

    }
}
