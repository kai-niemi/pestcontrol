package io.cockroachdb.pest.cluster;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import io.cockroachdb.pest.model.status.NodeStatus;

public interface StatusOperator extends Closeable {
    String clusterVersion() throws IOException;

    List<NodeStatus> nodeStatus() throws IOException;

    NodeStatus nodeStatusById(Integer id) throws IOException;
}
