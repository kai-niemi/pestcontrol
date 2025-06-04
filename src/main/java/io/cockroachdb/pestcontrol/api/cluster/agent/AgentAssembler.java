package io.cockroachdb.pestcontrol.api.cluster.agent;

import org.springframework.hateoas.server.RepresentationModelAssembler;

import io.cockroachdb.pestcontrol.api.LinkRelations;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class AgentAssembler implements RepresentationModelAssembler<AgentModel, AgentModel> {
    private final String clusterId;

    public AgentAssembler(String clusterId) {
        this.clusterId = clusterId;
    }

    @Override
    public AgentModel toModel(AgentModel resource) {
        if (resource.hasLinks()) {
            return resource;
        }

        resource.add(linkTo(methodOn(AgentController.class)
                .getAgent(clusterId))
                .withSelfRel());
        resource.add(linkTo(methodOn(AgentController.class)
                .startNode(clusterId, null, null))
                .withRel(LinkRelations.AGENT_START_REL));
        resource.add(linkTo(methodOn(AgentController.class)
                .stopNode(clusterId, null, null))
                .withRel(LinkRelations.AGENT_STOP_REL));

        return resource;
    }
}
