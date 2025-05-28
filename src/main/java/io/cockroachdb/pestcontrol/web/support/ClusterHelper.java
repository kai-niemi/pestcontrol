package io.cockroachdb.pestcontrol.web.support;

import io.cockroachdb.pestcontrol.api.cluster.status.StatusModel;
import io.cockroachdb.pestcontrol.schema.NodeStatus;

public class ClusterHelper {
    private boolean available;

    private StatusModel statusModel;

    public ClusterHelper(boolean available) {
        this.available = available;
    }

    public String getId() {
        return statusModel.getId();
    }

    public StatusModel getClusterModel() {
        return statusModel;
    }

    public boolean isDifferent(StatusModel statusModel) {
        int currentNodeSize = this.statusModel.getNodes().getContent().size();
        int nodeSize = statusModel.getNodes().getContent().size();
        return currentNodeSize != nodeSize;
    }

    public ClusterHelper setClusterModel(StatusModel statusModel) {
        this.statusModel = statusModel;
        return this;
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
