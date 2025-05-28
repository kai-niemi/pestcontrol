package io.cockroachdb.pestcontrol.api.cluster.machine;

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import io.cockroachdb.pestcontrol.api.LinkRelations;
import io.cockroachdb.pestcontrol.model.MachineProperties;

@Relation(value = LinkRelations.CLUSTER_MACHINE_REL,
        collectionRelation = LinkRelations.CLUSTER_MACHINES_REL)
@JsonPropertyOrder({"links", "embedded", "templates"})
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
public class MachinesForm extends RepresentationModel<MachinesForm> {
    @NotNull(message = "required field")
    @Size(min = 1, message = "1-based node id")
    private Integer nodeId;

    @NotNull(message = "required field")
    private String clusterId;

    @NotEmpty(message = "required collection")
    private List<@Valid MachineProperties> machines = new ArrayList<>();

    public Integer getNodeId() {
        return nodeId;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public List<MachineProperties> getMachines() {
        return machines;
    }

    public void setMachines(List<MachineProperties> machines) {
        this.machines = machines;
    }
}
