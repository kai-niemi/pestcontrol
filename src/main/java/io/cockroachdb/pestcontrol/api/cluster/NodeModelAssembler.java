package io.cockroachdb.pestcontrol.api.cluster;

import java.util.EnumSet;

import org.springframework.hateoas.server.RepresentationModelAssembler;

import io.cockroachdb.pestcontrol.api.LinkRelations;
import io.cockroachdb.pestcontrol.model.ClusterType;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class NodeModelAssembler
        implements RepresentationModelAssembler<NodeModel, NodeModel> {
    private final ClusterType clusterType;

    public NodeModelAssembler(ClusterType clusterType) {
        this.clusterType = clusterType;
    }

    @Override
    public NodeModel toModel(NodeModel resource) {
        if (resource.hasLinks()) {
            return resource;
        }

        resource.add(linkTo(methodOn(NodeController.class)
                .getNode(resource.getClusterId(), resource.getId()))
                .withSelfRel());
        resource.add(linkTo(methodOn(NodeController.class)
                .getNodeDetail(resource.getClusterId(), resource.getId()))
                .withRel(LinkRelations.NODE_DETAIL_REL)
                .withTitle("Node details and statistics"));
        resource.add(linkTo(methodOn(NodeController.class)
                .getNodeStatus(resource.getClusterId(), resource.getId()))
                .withRel(LinkRelations.NODE_STATUS_REL)
                .withTitle("Node status and liveness metrics"));

        if (EnumSet.of(ClusterType.cloud_dedicated, ClusterType.cloud_serverless,
                        ClusterType.cloud_standard)
                .contains(clusterType)) {
            resource.add(linkTo(methodOn(AdminController.class)
                    .disruptLocality(resource.getClusterId(), null))
                    .withRel(LinkRelations.NODE_DISRUPT_REL)
                    .withTitle("Apply locality disruption"));
            resource.add(linkTo(methodOn(AdminController.class)
                    .recoverLocality(resource.getClusterId(), null))
                    .withRel(LinkRelations.NODE_RECOVER_REL)
                    .withTitle("Recover locality disruption"));
        }

        resource.add(linkTo(methodOn(AdminController.class)
                .disruptNode(resource.getClusterId(), resource.getId()))
                .withRel(LinkRelations.NODE_DISRUPT_REL)
                .withTitle("Apply node disruption"));
        resource.add(linkTo(methodOn(AdminController.class)
                .recoverNode(resource.getClusterId(), resource.getId()))
                .withRel(LinkRelations.NODE_RECOVER_REL)
                .withTitle("Recover node disruption"));

        return resource;
    }
}
