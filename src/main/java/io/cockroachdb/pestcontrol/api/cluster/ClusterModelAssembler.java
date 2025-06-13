package io.cockroachdb.pestcontrol.api.cluster;

import java.util.EnumSet;

import org.springframework.hateoas.server.RepresentationModelAssembler;

import io.cockroachdb.pestcontrol.api.LinkRelations;
import io.cockroachdb.pestcontrol.model.ClusterType;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class ClusterModelAssembler
        implements RepresentationModelAssembler<ClusterModel, ClusterModel> {
    @Override
    public ClusterModel toModel(ClusterModel resource) {
        String clusterId = resource.getClusterProperties().getClusterId();

        resource.add(linkTo(methodOn(ClusterController.class)
                .getCluster(clusterId))
                .withSelfRel());
        resource.add(linkTo(methodOn(ClusterController.class)
                .getVersion(clusterId))
                .withRel(LinkRelations.VERSION_REL));
        resource.add(linkTo(methodOn(AdminController.class)
                .adminForm(clusterId))
                .withRel(LinkRelations.CLUSTER_ADMIN_REL));
        resource.add(linkTo(methodOn(NodeController.class)
                .getNodes(clusterId))
                .withRel(LinkRelations.NODE_COLL_REL));
        resource.add(linkTo(methodOn(WorkloadController.class)
                .getWorkloads(clusterId))
                .withRel(LinkRelations.WORKLOAD_COLL_REL));

        if (EnumSet.of(ClusterType.hosted_insecure, ClusterType.hosted_secure)
                .contains(resource.getClusterProperties().getClusterType())) {
            resource.add(linkTo(methodOn(HostedController.class)
                    .hostedForm(clusterId))
                    .withRel(LinkRelations.HOSTED_REL));
        }

        return resource;
    }
}
