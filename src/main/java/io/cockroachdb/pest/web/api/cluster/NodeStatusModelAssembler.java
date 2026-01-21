package io.cockroachdb.pest.web.api.cluster;

import org.springframework.hateoas.server.RepresentationModelAssembler;

import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.model.status.NodeStatus;
import io.cockroachdb.pest.web.model.NodeStatusModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class NodeStatusModelAssembler implements RepresentationModelAssembler<NodeStatus, NodeStatusModel> {
    private final Cluster cluster;

    public NodeStatusModelAssembler(Cluster cluster) {
        this.cluster = cluster;
    }

    @Override
    public NodeStatusModel toModel(NodeStatus nodeStatus) {
        NodeStatusModel model = new NodeStatusModel(nodeStatus);
        model.add(linkTo(methodOn(ClusterController.class)
                .getNodeStatusById(cluster.getClusterId(), nodeStatus.getDesc().getNodeId()))
                .withSelfRel());
        return model;
    }
}
