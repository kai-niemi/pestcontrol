package io.cockroachdb.pest.api.cluster;

import java.util.EnumSet;

import org.springframework.hateoas.server.RepresentationModelAssembler;

import io.cockroachdb.pest.api.LinkRelations;
import io.cockroachdb.pest.model.ClusterType;
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
                    .withRel(LinkRelations.LOCALITY_DISRUPT_REL));
            resource.add(linkTo(methodOn(AdminController.class)
                    .recoverLocality(resource.getClusterId(), null))
                    .withRel(LinkRelations.LOCALITY_RECOVER_REL));
        } else if (EnumSet.of(ClusterType.hosted_insecure, ClusterType.hosted_secure)
                .contains(clusterType)) {
            resource.add(linkTo(methodOn(AdminController.class)
                    .startNode(resource.getClusterId(), resource.getId()))
                    .withRel(LinkRelations.NODE_START_REL));
            resource.add(linkTo(methodOn(AdminController.class)
                    .stopNode(resource.getClusterId(), resource.getId()))
                    .withRel(LinkRelations.NODE_STOP_REL));
            resource.add(linkTo(methodOn(AdminController.class)
                    .killNode(resource.getClusterId(), resource.getId()))
                    .withRel(LinkRelations.NODE_KILL_REL));
            resource.add(linkTo(methodOn(AdminController.class)
                    .initNode(resource.getClusterId(), resource.getId()))
                    .withRel(LinkRelations.NODE_INIT_REL));
            resource.add(linkTo(methodOn(AdminController.class)
                    .installNode(resource.getClusterId(), resource.getId()))
                    .withRel(LinkRelations.NODE_INSTALL_REL));
        }

        resource.add(linkTo(methodOn(AdminController.class)
                .disruptNode(resource.getClusterId(), resource.getId()))
                .withRel(LinkRelations.DISRUPT_NODE_REL));
        resource.add(linkTo(methodOn(AdminController.class)
                .recoverNode(resource.getClusterId(), resource.getId()))
                .withRel(LinkRelations.RECOVER_NODE_REL));

        return resource;
    }
}
