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
    public String queryClusterVersion() {
        return "";
    }

    @Override
    public NodeModel queryNodeById(Integer id) {
        return null;
    }

    @Override
    public List<NodeModel> queryAllNodes() {
        return List.of();
    }

    @Override
    public NodeDetail queryNodeDetailById(Integer id) {
        return null;
    }

    @Override
    public NodeStatus queryNodeStatusById(Integer id) {
        return null;
    }
}
