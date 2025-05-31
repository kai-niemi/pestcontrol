package io.cockroachdb.pestcontrol.api.cluster.machine;

import org.springframework.hateoas.server.RepresentationModelAssembler;

import io.cockroachdb.pestcontrol.api.LinkRelations;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class MachineModelAssembler
        implements RepresentationModelAssembler<MachineModel, MachineModel> {
    @Override
    public MachineModel toModel(MachineModel resource) {
        if (resource.hasLinks()) {
            return resource;
        }

        resource.add(linkTo(methodOn(MachineController.class)
                .getClusterIndex(resource.getClusterId()))
                .withRel(LinkRelations.CLUSTER_MACHINE_REL));
        resource.add(linkTo(methodOn(MachineController.class)
                .startMachine(null, null))
                .withRel(LinkRelations.START_REL));
        resource.add(linkTo(methodOn(MachineController.class)
                .stopMachine(null, null))
                .withRel(LinkRelations.START_REL));

        return resource;
    }
}
