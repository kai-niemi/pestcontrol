package io.cockroachdb.pestcontrol.web.support;

import org.springframework.hateoas.CollectionModel;

import io.cockroachdb.pestcontrol.api.cluster.NodeModel;
import io.cockroachdb.pestcontrol.schema.NodeStatus;

public class ClusterHelper {
    private final String clusterId;

    private boolean available;

    private CollectionModel<NodeModel> nodeModels = CollectionModel.empty();

    public ClusterHelper(String clusterId, boolean available) {
        this.clusterId = clusterId;
        this.available = available;
    }

    public String getId() {
        return clusterId;
    }

    public CollectionModel<NodeModel> getNodeModels() {
        return nodeModels;
    }

    public void setNodeModels(
            CollectionModel<NodeModel> nodeModels) {
        this.nodeModels = nodeModels;
    }

    public boolean isDifferent(CollectionModel<NodeModel> otherModel) {
        int currentNodeSize = this.nodeModels.getContent().size();
        int nodeSize = otherModel.getContent().size();
        return currentNodeSize != nodeSize;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getCardClass(NodeStatus nodeStatus) {
        if (!isAvailable()) {
            return "border-warning alert-warning";
        }

        boolean isLive = "true".equals(nodeStatus.getIsLive());  // up and running
        boolean isAvailable = "true".equals(nodeStatus.getIsAvailable()); // part of quorum
        if (isLive) {
            if (isAvailable) {
                return "border-success alert-success";
            }
            return "border-warning alert-warning";
        } else {
            if (isAvailable) {
                return "border-warning alert-warning";
            }
            return "border-danger alert-danger";
        }
    }

    public String getCardImage(NodeStatus nodeStatus) {
        if (!isAvailable()) {
            return "#db-warn";
        }

        boolean isLive = "true".equals(nodeStatus.getIsLive());  // up and running
        boolean isAvailable = "true".equals(nodeStatus.getIsAvailable()); // part of quorum
        if (isLive) {
            if (isAvailable) {
                return "#db-ok";
            }
            return "#db-warn";
        } else {
            if (isAvailable) {
                return "#db-warn";
            }
            return "#db-fail";
        }
    }

}
