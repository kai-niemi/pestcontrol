package io.cockroachdb.pestcontrol.api.cluster;

import java.util.EnumSet;

import org.springframework.hateoas.server.RepresentationModelAssembler;

import io.cockroachdb.pestcontrol.api.LinkRelations;
import io.cockroachdb.pestcontrol.model.ClusterType;
import io.cockroachdb.pestcontrol.schema.NodeModel;
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

        resource.add(linkTo(methodOn(ClusterStatusController.class)
                .getNode(resource.getClusterId(), resource.getId()))
                .withSelfRel());
        resource.add(linkTo(methodOn(ClusterStatusController.class)
                .getNodeDetail(resource.getClusterId(), resource.getId()))
                .withRel(LinkRelations.NODE_DETAIL_REL)
                .withTitle("Node details and statistics"));
        resource.add(linkTo(methodOn(ClusterStatusController.class)
                .getNodeStatus(resource.getClusterId(), resource.getId()))
                .withRel(LinkRelations.NODE_STATUS_REL)
                .withTitle("Node status and liveness metrics"));

        if (EnumSet.of(ClusterType.local_insecure, ClusterType.local_secure)
                .contains(clusterType)) {
            resource.add(linkTo(methodOn(ClusterAdminController.class)
                    .disruptNode(resource.getClusterId(), resource.getId()))
                    .withRel(LinkRelations.DISRUPT_REL)
                    .withTitle("Apply node disruption"));
            resource.add(linkTo(methodOn(ClusterAdminController.class)
                    .recoverNode(resource.getClusterId(), resource.getId()))
                    .withRel(LinkRelations.RECOVER_REL)
                    .withTitle("Recover node disruption"));
        }

        if (EnumSet.of(ClusterType.cloud_dedicated, ClusterType.cloud_serverless,
                        ClusterType.cloud_standard)
                .contains(clusterType)) {
            resource.add(linkTo(methodOn(ClusterAdminController.class)
                    .disruptLocalityTier(resource.getClusterId(), null))
                    .withRel(LinkRelations.DISRUPT_REL)
                    .withTitle("Apply locality disruption"));

            resource.add(linkTo(methodOn(ClusterAdminController.class)
                    .recoverLocalityTier(resource.getClusterId(), null))
                    .withRel(LinkRelations.RECOVER_REL)
                    .withTitle("Recover locality disruption"));
        }

        return resource;
    }
}
