package io.cockroachdb.pestcontrol.api.cluster;

import org.springframework.hateoas.server.RepresentationModelAssembler;

import io.cockroachdb.pestcontrol.api.LinkRelations;
import io.cockroachdb.pestcontrol.api.cluster.machine.MachineController;
import io.cockroachdb.pestcontrol.api.cluster.status.StatusController;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class ClusterModelAssembler
        implements RepresentationModelAssembler<ClusterModel, ClusterModel> {
    @Override
    public ClusterModel toModel(ClusterModel resource) {
        String id = resource.getClusterProperties().getClusterId();
        resource.add(linkTo(methodOn(ClusterController.class)
                .getCluster(id))
                .withSelfRel());
        resource.add(linkTo(methodOn(StatusController.class)
                .getCluster(id))
                .withRel(LinkRelations.CLUSTER_STATUS_REL));
        resource.add(linkTo(methodOn(MachineController.class)
                .getMachines(id))
                .withRel(LinkRelations.CLUSTER_MACHINES_REL));
        return resource;
    }
}
