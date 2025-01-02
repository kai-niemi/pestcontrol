package io.cockroachdb.pc.web.api.cluster;

import org.springframework.hateoas.server.RepresentationModelAssembler;

import io.cockroachdb.pc.schema.ClusterType;
import io.cockroachdb.pc.schema.LocalityModel;
import io.cockroachdb.pc.schema.nodes.Locality;
import io.cockroachdb.pc.web.api.LinkRelations;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class LocalityModelAssembler implements RepresentationModelAssembler<Locality, LocalityModel> {
    private final ClusterType clusterType;

    private final String clusterId;

    public LocalityModelAssembler(String clusterId, ClusterType clusterType) {
        this.clusterId = clusterId;
        this.clusterType = clusterType;
    }

    @Override
    public LocalityModel toModel(Locality locality) {
        LocalityModel model = new LocalityModel(locality.getTiers());

        model.add(linkTo(methodOn(LocalityRestController.class)
                .getLocality(clusterId, locality.toTiers()))
                .withSelfRel());

        if (ClusterType.cloud_dedicated.equals(clusterType)) {
            locality.findRegionTierValue().ifPresent(name -> {
                model.add(linkTo(methodOn(LocalityRestController.class)
                        .disruptLocalityTier(clusterId, locality.toTiers()))
                        .withRel(LinkRelations.DISRUPT_REL)
                        .withTitle("Apply locality tier disruption"));
                model.add(linkTo(methodOn(LocalityRestController.class)
                        .recoverLocalityTier(clusterId, locality.toTiers()))
                        .withRel(LinkRelations.RECOVER_REL)
                        .withTitle("Recover locality tier disruption"));
            });
        }

        return model;
    }
}
