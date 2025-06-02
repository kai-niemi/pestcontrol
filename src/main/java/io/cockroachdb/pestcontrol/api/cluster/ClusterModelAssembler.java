package io.cockroachdb.pestcontrol.api.cluster;

import java.util.EnumSet;

import org.springframework.hateoas.server.RepresentationModelAssembler;

import io.cockroachdb.pestcontrol.api.LinkRelations;
import io.cockroachdb.pestcontrol.api.cluster.admin.AdminController;
import io.cockroachdb.pestcontrol.api.cluster.network.NetworkController;
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
        resource.add(linkTo(methodOn(ClusterController.class)
                .getVersion(id))
                .withRel(LinkRelations.VERSION_REL));
        resource.add(linkTo(methodOn(NodeController.class)
                .getNodes(id))
                .withRel(LinkRelations.CLUSTER_NODE_COLL_REL));
        resource.add(linkTo(methodOn(AdminController.class)
                .getAdmin(id))
                .withRel(LinkRelations.CLUSTER_ADMIN_REL));
        resource.add(linkTo(methodOn(WorkloadController.class)
                .getClusterIndex(id))
                .withRel(LinkRelations.WORKLOAD_COLL_REL));

        if (EnumSet.of(ClusterType.remote_insecure, ClusterType.remote_secure)
                .contains(resource.getClusterProperties().getClusterType())) {
            resource.add(linkTo(methodOn(NetworkController.class)
                    .getNetwork(id))
                    .withRel(LinkRelations.NETWORK_REL));
        }

        return resource;
    }
}
