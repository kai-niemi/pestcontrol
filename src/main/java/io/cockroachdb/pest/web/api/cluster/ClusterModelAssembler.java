package io.cockroachdb.pest.web.api.cluster;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;

import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.model.ClusterTypes;
import io.cockroachdb.pest.model.LinkRelations;
import io.cockroachdb.pest.web.model.ClusterModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class ClusterModelAssembler implements RepresentationModelAssembler<Cluster, ClusterModel> {
    @Override
    public ClusterModel toModel(Cluster entity) {
        try {
            ClusterModel clusterModel = new ClusterModel(entity);
            clusterModel
                    .add(linkTo(methodOn(ClusterController.class)
                            .getCluster(entity.getClusterId()))
                            .withSelfRel())
                    .add(Link.of(entity.getAdminUrl())
                            .withRel(LinkRelations.ADMIN_REL))
                    .add(linkTo(methodOn(ClusterController.class)
                            .getVersion(entity.getClusterId()))
                            .withRel(LinkRelations.VERSION_REL))
                    .add(linkTo(methodOn(ClusterController.class)
                            .getNodeStatus(entity.getClusterId()))
                            .withRel(LinkRelations.STATUS_REL))
                    .add(linkTo(methodOn(ClusterController.class)
                            .getNodeStatusById(entity.getClusterId(), null)) // templated
                            .withRel(LinkRelations.STATUS_REL));

            if (ClusterTypes.isHosted(entity.getClusterType())) {
                clusterModel.add(linkTo(methodOn(LocalOperatorController.class)
                        .index())
                        .withRel(LinkRelations.OPERATOR_REL));
            } else if (ClusterTypes.isCloud(entity.getClusterType())) {
                clusterModel.add(linkTo(methodOn(CloudOperatorController.class)
                        .index())
                        .withRel(LinkRelations.OPERATOR_REL));
            }

            return clusterModel;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
