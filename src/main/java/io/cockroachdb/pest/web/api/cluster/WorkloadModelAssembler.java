package io.cockroachdb.pest.web.api.cluster;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import io.cockroachdb.pest.web.LinkRelations;
import io.cockroachdb.pest.workload.model.Workload;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class WorkloadModelAssembler implements RepresentationModelAssembler<Workload, Workload> {
    @Override
    public Workload toModel(Workload resource) {
        if (resource.hasLinks()) {
            return resource;
        }

        Link selfLink = linkTo(methodOn(WorkloadController.class)
                .getWorker(resource.getClusterId(), resource.getId()))
                .withSelfRel();

        if (resource.isRunning()) {
            selfLink = selfLink.andAffordance(afford(methodOn(WorkloadController.class)
                    .cancelWorker(resource.getClusterId(), resource.getId())));

            resource.add(linkTo(methodOn(WorkloadController.class)
                    .cancelWorker(resource.getClusterId(), resource.getId()))
                    .withRel(LinkRelations.CANCEL_REL));
        } else {
            selfLink = selfLink.andAffordance(afford(methodOn(WorkloadController.class)
                    .deleteWorker(resource.getClusterId(), resource.getId())));

            resource.add(linkTo(methodOn(WorkloadController.class)
                    .deleteWorker(resource.getClusterId(), resource.getId()))
                    .withRel(LinkRelations.DELETE_REL));
        }

        resource.add(selfLink);

        return resource;
    }
}
