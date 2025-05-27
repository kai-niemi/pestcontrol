package io.cockroachdb.pestcontrol.api.machine;

import org.springframework.hateoas.server.RepresentationModelAssembler;

import io.cockroachdb.pestcontrol.api.LinkRelations;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class MachineFormAssembler
        implements RepresentationModelAssembler<MachinesForm, MachinesForm> {
    @Override
    public MachinesForm toModel(MachinesForm entity) {
        MachinesForm form = new MachinesForm();
        form.add(linkTo(methodOn(MachinesController.class)
                .getForm(entity.getClusterId()))
                .withRel(LinkRelations.MACHINE_REL));
        form.add(linkTo(methodOn(MachinesController.class)
                .startMachine(null))
                .withRel("start"));
        form.add(linkTo(methodOn(MachinesController.class)
                .stopMachine(null))
                .withRel("stop"));
        return form;
    }
}
