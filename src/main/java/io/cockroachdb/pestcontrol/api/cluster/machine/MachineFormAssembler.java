package io.cockroachdb.pestcontrol.api.cluster.machine;

import org.springframework.hateoas.server.RepresentationModelAssembler;

import io.cockroachdb.pestcontrol.api.LinkRelations;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class MachineFormAssembler
        implements RepresentationModelAssembler<MachinesForm, MachinesForm> {
    @Override
    public MachinesForm toModel(MachinesForm entity) {
        MachinesForm form = new MachinesForm();
        form.add(linkTo(methodOn(MachineController.class)
                .getMachines(entity.getClusterId()))
                .withRel(LinkRelations.CLUSTER_MACHINE_REL));
        form.add(linkTo(methodOn(MachineController.class)
                .startMachine(null))
                .withRel("start"));
        form.add(linkTo(methodOn(MachineController.class)
                .stopMachine(null))
                .withRel("stop"));
        return form;
    }
}
