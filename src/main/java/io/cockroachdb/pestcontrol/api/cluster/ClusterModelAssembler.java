package io.cockroachdb.pestcontrol.api.cluster;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;

import io.cockroachdb.pestcontrol.api.LinkRelations;
import io.cockroachdb.pestcontrol.api.workload.WorkloadController;
import io.cockroachdb.pestcontrol.schema.ClusterModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class ClusterModelAssembler
        implements RepresentationModelAssembler<ClusterModel, ClusterModel> {
    @Override
    public ClusterModel toModel(ClusterModel resource) {
        if (resource.hasLinks()) {
            return resource;
        }

        resource.add(linkTo(methodOn(ClusterController.class)
                .getCluster(resource.getId()))
                .withSelfRel());
        resource.add(linkTo(methodOn(ClusterStatusController.class)
                .getVersion(resource.getId()))
                .withRel(LinkRelations.VERSION_REL)
                .withTitle("CockroachDB cluster version"));
        resource.add(linkTo(methodOn(ClusterStatusController.class)
                .getNodes(resource.getId()))
                .withRel(LinkRelations.NODES_REL)
                .withTitle("Collection of cluster nodes"));
        resource.add(linkTo(methodOn(WorkloadController.class)
                .getWorkers(resource.getId()))
                .withRel(LinkRelations.WORKLOADS_REL)
                .withTitle("Collection of cluster workers"));
        resource.add(Link.of(resource.getClusterProperties().getAdminUrl())
                .withRel(LinkRelations.ADMIN_REL)
                .withTitle("CockroachDB DB Console"));

        return resource;
    }
}
