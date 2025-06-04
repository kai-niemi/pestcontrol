package io.cockroachdb.pestcontrol.api.cluster.agent;

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import io.cockroachdb.pestcontrol.api.LinkRelations;
import io.cockroachdb.pestcontrol.model.NodeProperties;

@Relation(value = LinkRelations.AGENT_MODEL)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY,
        content = JsonInclude.Include.NON_NULL)
@Validated
public class AgentModel extends RepresentationModel<AgentModel> {
    @NotEmpty
    private List<@Valid NodeProperties> nodes = new ArrayList<>();

    public List<NodeProperties> getNodes() {
        return nodes;
    }

    public void setNodes(List<NodeProperties> nodes) {
        this.nodes = nodes;
    }

    public NodeProperties findNodeProperties(Integer nodeId) {
        return nodes.stream()
                .filter(agent -> nodeId.equals(agent.getId()))
                .findFirst()
                .orElseThrow();
    }
}
