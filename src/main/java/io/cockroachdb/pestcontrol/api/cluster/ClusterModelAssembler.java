package io.cockroachdb.pestcontrol.api.cluster;

import java.util.EnumSet;

import org.springframework.hateoas.server.RepresentationModelAssembler;

import io.cockroachdb.pestcontrol.api.LinkRelations;
import io.cockroachdb.pestcontrol.api.cluster.admin.AdminController;
import io.cockroachdb.pestcontrol.api.cluster.machine.MachineController;
import io.cockroachdb.pestcontrol.api.cluster.status.StatusController;
import io.cockroachdb.pestcontrol.api.cluster.workload.WorkloadController;
import io.cockroachdb.pestcontrol.model.ClusterType;
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
                .getClusterIndex(id))
                .withRel(LinkRelations.CLUSTER_STATUS_REL));
        resource.add(linkTo(methodOn(AdminController.class)
                .getClusterIndex(id))
                .withRel(LinkRelations.CLUSTER_ADMIN_REL));
        resource.add(linkTo(methodOn(WorkloadController.class)
                .getClusterIndex(id))
                .withRel(LinkRelations.WORKLOAD_COLL_REL));

        if (EnumSet.of(ClusterType.remote_insecure, ClusterType.remote_secure)
                .contains(resource.getClusterProperties().getClusterType())) {
            resource.add(linkTo(methodOn(MachineController.class)
                    .getClusterIndex(id))
                    .withRel(LinkRelations.CLUSTER_MACHINE_REL));
        }

        return resource;
    }
}
