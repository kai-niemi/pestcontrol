package io.cockroachdb.pest.web.api.cluster;

import org.springframework.hateoas.server.RepresentationModelAssembler;

import io.cockroachdb.pest.cluster.model.NodeModel;
import io.cockroachdb.pest.web.LinkRelations;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class NodeModelAssembler
        implements RepresentationModelAssembler<NodeModel, NodeModel> {
    public NodeModelAssembler() {
    }

    @Override
    public NodeModel toModel(NodeModel resource) {
        if (resource.hasLinks()) {
            return resource;
        }

        resource.add(linkTo(methodOn(NodeController.class)
                .getNode(resource.getClusterId(), resource.getNodeId()))
                .withSelfRel());
        resource.add(linkTo(methodOn(NodeController.class)
                .getNodeDetail(resource.getClusterId(), resource.getNodeId()))
                .withRel(LinkRelations.NODE_DETAIL_REL));
        resource.add(linkTo(methodOn(NodeController.class)
                .getNodeStatus(resource.getClusterId(), resource.getNodeId()))
                .withRel(LinkRelations.NODE_STATUS_REL));

        return resource;
    }
}
