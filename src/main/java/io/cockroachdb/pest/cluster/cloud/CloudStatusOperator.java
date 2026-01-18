package io.cockroachdb.pest.cluster.cloud;

import java.util.List;

import io.cockroachdb.pest.cluster.StatusOperator;
import io.cockroachdb.pest.cluster.model.NodeDetail;
import io.cockroachdb.pest.cluster.model.NodeModel;
import io.cockroachdb.pest.cluster.model.NodeStatus;

public class CloudStatusOperator implements StatusOperator {
    @Override
    public void close() {

    }

    @Override
    public String clusterVersion() {
        return "";
    }

    @Override
    public List<NodeModel> listAllNodes() {
        return List.of();
    }

    @Override
    public NodeDetail nodeDetailById(Integer id) {
        return null;
    }

    @Override
    public NodeStatus nodeStatusById(Integer id) {
        return null;
    }
}
