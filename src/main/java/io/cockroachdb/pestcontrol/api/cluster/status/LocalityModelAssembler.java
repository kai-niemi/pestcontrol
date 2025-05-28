package io.cockroachdb.pestcontrol.api.cluster.status;

import java.util.EnumSet;

import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import io.cockroachdb.pestcontrol.api.LinkRelations;
import io.cockroachdb.pestcontrol.model.ClusterType;
import io.cockroachdb.pestcontrol.model.Locality;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class LocalityModelAssembler
        implements RepresentationModelAssembler<Locality, LocalityModel> {
    private final ClusterType clusterType;

    private final String clusterId;

    public LocalityModelAssembler(String clusterId, ClusterType clusterType) {
        this.clusterId = clusterId;
        this.clusterType = clusterType;
    }

    @Override
    public LocalityModel toModel(Locality locality) {
        LocalityModel model = new LocalityModel(locality.getTiers());

        model.add(WebMvcLinkBuilder.linkTo(methodOn(StatusController.class)
                .getLocality(clusterId, locality.toTiers()))
                .withSelfRel());

        if (EnumSet.of(ClusterType.cloud_dedicated,
                        ClusterType.cloud_serverless,
                        ClusterType.cloud_standard)
                .contains(clusterType)) {
            locality.findRegionTierValue().ifPresent(name -> {
                model.add(linkTo(methodOn(StatusController.class)
                        .disruptLocality(clusterId, locality.toTiers()))
                        .withRel(LinkRelations.DISRUPT_REL)
                        .withTitle("Apply locality tier disruption"));
                model.add(linkTo(methodOn(StatusController.class)
                        .recoverLocality(clusterId, locality.toTiers()))
                        .withRel(LinkRelations.RECOVER_REL)
                        .withTitle("Recover locality tier disruption"));
            });
        }

        return model;
    }
}
