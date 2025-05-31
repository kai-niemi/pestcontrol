package io.cockroachdb.pestcontrol.api.cluster.admin;

import java.util.EnumSet;

import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import io.cockroachdb.pestcontrol.api.LinkRelations;
import io.cockroachdb.pestcontrol.api.cluster.machine.MachineController;
import io.cockroachdb.pestcontrol.model.ClusterType;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class AdminModelAssembler
        implements RepresentationModelAssembler<AdminModel, AdminModel> {
    private final ClusterType clusterType;

    private final String clusterId;

    public AdminModelAssembler(String clusterId, ClusterType clusterType) {
        this.clusterId = clusterId;
        this.clusterType = clusterType;
    }

    @Override
    public AdminModel toModel(AdminModel resource) {
        if (resource.hasLinks()) {
            return resource;
        }

        resource.add(WebMvcLinkBuilder.linkTo(methodOn(AdminController.class)
                        .getClusterIndex(clusterId))
                .withSelfRel());

        if (EnumSet.of(ClusterType.cloud_dedicated,
                        ClusterType.cloud_serverless,
                        ClusterType.cloud_standard)
                .contains(clusterType)) {
            resource.add(linkTo(methodOn(AdminController.class)
                    .disruptLocality(resource.getClusterId(), null))
                    .withRel(LinkRelations.DISRUPT_LOC_REL)
                    .withTitle("Apply locality disruption"));
            resource.add(linkTo(methodOn(AdminController.class)
                    .recoverLocality(resource.getClusterId(), null))
                    .withRel(LinkRelations.RECOVER_LOC_REL)
                    .withTitle("Recover locality disruption"));
        }

        resource.add(linkTo(methodOn(AdminController.class)
                .disruptNode(resource.getClusterId(), null))
                .withRel(LinkRelations.DISRUPT_REL)
                .withTitle("Apply node disruption"));
        resource.add(linkTo(methodOn(AdminController.class)
                .recoverNode(resource.getClusterId(), null))
                .withRel(LinkRelations.RECOVER_REL)
                .withTitle("Recover node disruption"));

        return resource;
    }
}
