package io.cockroachdb.pestcontrol.api.cluster.status;

import org.springframework.hateoas.server.RepresentationModelAssembler;

import io.cockroachdb.pestcontrol.api.LinkRelations;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class StatusModelAssembler
        implements RepresentationModelAssembler<StatusModel, StatusModel> {
    @Override
    public StatusModel toModel(StatusModel resource) {
        if (resource.hasLinks()) {
            return resource;
        }

        resource.add(linkTo(methodOn(StatusController.class)
                .getClusterIndex(resource.getId()))
                .withSelfRel());
        resource.add(linkTo(methodOn(StatusController.class)
                .getVersion(resource.getId()))
                .withRel(LinkRelations.VERSION_REL)
                .withTitle("CockroachDB version"));
        resource.add(linkTo(methodOn(StatusController.class)
                .getNodes(resource.getId()))
                .withRel(LinkRelations.NODES_REL)
                .withTitle("Collection of cluster nodes"));

        return resource;
    }
}
