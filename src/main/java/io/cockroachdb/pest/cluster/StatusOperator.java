package io.cockroachdb.pest.cluster;

import java.io.Closeable;
import java.util.List;

import io.cockroachdb.pest.cluster.model.NodeDetail;
import io.cockroachdb.pest.cluster.model.NodeModel;
import io.cockroachdb.pest.cluster.model.NodeStatus;

public interface StatusOperator extends Closeable {
    String clusterVersion();

    List<NodeModel> listAllNodes();

    NodeDetail nodeDetailById(Integer id);

    NodeStatus nodeStatusById(Integer id);
}
