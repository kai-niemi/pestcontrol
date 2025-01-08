package io.cockroachdb.pc.service;

import java.util.EnumSet;

import org.springframework.stereotype.Component;

import io.cockroachdb.pc.model.ClusterProperties;
import io.cockroachdb.pc.schema.ClusterType;
import io.cockroachdb.pc.schema.NodeModel;
import io.cockroachdb.pc.schema.nodes.Locality;

@Component
public class RemoteDisruptionManager implements DisruptionManager {
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
