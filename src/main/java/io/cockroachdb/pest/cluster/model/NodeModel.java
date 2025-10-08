package io.cockroachdb.pest.cluster.model;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Combination of node detail and status.
 */
@Relation(value = "node", collectionRelation = "nodes")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NodeModel extends RepresentationModel<NodeModel> {
    private String clusterId;

    @JsonProperty("detail")
    private NodeDetail nodeDetail;

    @JsonProperty("status")
    private NodeStatus nodeStatus;

    public NodeModel(String clusterId, NodeDetail nodeDetail, NodeStatus nodeStatus) {
        Assert.notNull(nodeDetail, "nodeDetail is null");
        Assert.notNull(nodeDetail.getNodeId(), "nodeDetail.id is null");
        Assert.notNull(nodeDetail.getLocality(), "nodeDetail.locality is null");
        Assert.notNull(nodeDetail.getSqlAddress(), "nodeDetail.aqlAddress");
        Assert.notNull(nodeStatus, "nodeStatus is null");

        this.clusterId = clusterId;
        this.nodeDetail = nodeDetail;
        this.nodeStatus = nodeStatus;
    }

    public String getClusterId() {
        return clusterId;
    }

    public Integer getNodeId() {
        return nodeDetail.getNodeId();
    }

    public String getDescription() {
        return "Node " + getNodeId()
               + ", " + nodeDetail.getLocality()
               + ", " + nodeDetail.getSqlAddress().getAddressField();
    }

    public NodeDetail getNodeDetail() {
        return nodeDetail;
    }

    public NodeStatus getNodeStatus() {
        return nodeStatus;
    }
}
