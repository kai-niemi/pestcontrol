package io.cockroachdb.pestcontrol.api.cluster.machine;

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import io.cockroachdb.pestcontrol.api.LinkRelations;
import io.cockroachdb.pestcontrol.model.MachineProperties;

@Relation(value = LinkRelations.CLUSTER_MACHINE_REL,
        collectionRelation = LinkRelations.CLUSTER_MACHINE_COLL_REL)
@JsonPropertyOrder({"links", "embedded", "templates"})
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
@Validated
public class MachineModel extends RepresentationModel<MachineModel> {
    public static MachineModel fromId(String clusterId) {
        return new MachineModel(clusterId);
    }

    private final String clusterId;

    @NotEmpty(message = "required collection")
    private List<@Valid MachineProperties> machines = new ArrayList<>();

    public MachineModel(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public List<MachineProperties> getMachines() {
        return machines;
    }

    public void setMachines(List<MachineProperties> machines) {
        this.machines = machines;
    }
}
