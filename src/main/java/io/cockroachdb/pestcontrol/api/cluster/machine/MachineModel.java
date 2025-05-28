package io.cockroachdb.pestcontrol.api.cluster.machine;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.cockroachdb.pestcontrol.api.LinkRelations;
import io.cockroachdb.pestcontrol.model.MachineProperties;

@Relation(value = LinkRelations.CLUSTER_MACHINE_REL,
        collectionRelation = LinkRelations.CLUSTER_MACHINES_REL)
@JsonPropertyOrder({"links", "embedded", "templates"})
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
public class MachineModel extends RepresentationModel<MachineModel> {
    private MachineProperties machineProperties;

    public MachineModel(MachineProperties machineProperties) {
        this.machineProperties = machineProperties;
    }

    public MachineProperties getMachineProperties() {
        return machineProperties;
    }

    public void setMachineProperties(MachineProperties machineProperties) {
        this.machineProperties = machineProperties;
    }
}
