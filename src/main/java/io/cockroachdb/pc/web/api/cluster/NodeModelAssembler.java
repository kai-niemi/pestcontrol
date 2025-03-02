package io.cockroachdb.pc.web.api.cluster;

import java.util.EnumSet;

import org.springframework.hateoas.server.RepresentationModelAssembler;

import io.cockroachdb.pc.schema.ClusterType;
import io.cockroachdb.pc.schema.NodeModel;
import io.cockroachdb.pc.web.api.LinkRelations;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class NodeModelAssembler implements RepresentationModelAssembler<NodeModel, NodeModel> {
    private final ClusterType clusterType;

    public NodeModelAssembler(ClusterType clusterType) {
        this.clusterType = clusterType;
    }

    @Override
    public NodeModel toModel(NodeModel resource) {
        if (resource.hasLinks()) {
            return resource;
        }

        resource.add(linkTo(methodOn(NodeRestController.class)
                .getNode(resource.getClusterId(), resource.getId()))
                .withSelfRel());

        resource.add(linkTo(methodOn(NodeRestController.class)
                .getNodeDetail(resource.getClusterId(), resource.getId()))
                .withRel(LinkRelations.NODE_DETAIL_REL)
                .withTitle("Node details and statistics"));

        resource.add(linkTo(methodOn(NodeRestController.class)
                .getNodeStatus(resource.getClusterId(), resource.getId()))
                .withRel(LinkRelations.NODE_STATUS_REL)
                .withTitle("Node status and liveness metrics"));


        if (EnumSet.of(ClusterType.local_insecure, ClusterType.local_secure,
                        ClusterType.cloud_dedicated, ClusterType.cloud_serverless, ClusterType.cloud_standard)
                .contains(clusterType)) {
//            if ("true".equals(resource.getNodeStatus().getIsAvailable())) {
                resource.add(linkTo(methodOn(NodeRestController.class)
                        .disruptNode(resource.getClusterId(), resource.getId()))
                        .withRel(LinkRelations.DISRUPT_REL)
                        .withTitle("Apply node disruption"));
//            } else {
                resource.add(linkTo(methodOn(NodeRestController.class)
                        .recoverNode(resource.getClusterId(), resource.getId()))
                        .withRel(LinkRelations.RECOVER_REL)
                        .withTitle("Recover from node disruption"));
//            }
        }

        return resource;
    }
}
