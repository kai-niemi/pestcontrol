package io.cockroachdb.pc.web.api.workload;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import io.cockroachdb.pc.workload.model.Workload;
import io.cockroachdb.pc.web.api.LinkRelations;

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

        Link selfLink = linkTo(methodOn(WorkloadRestController.class)
                .getWorker(resource.getClusterId(), resource.getId()))
                .withSelfRel();

        if (resource.isRunning()) {
            selfLink = selfLink.andAffordance(afford(methodOn(WorkloadRestController.class)
                    .cancelWorker(resource.getClusterId(), resource.getId())));

            resource.add(linkTo(methodOn(WorkloadRestController.class)
                    .cancelWorker(resource.getClusterId(), resource.getId()))
                    .withRel(LinkRelations.CANCEL_REL));
        } else {
            selfLink = selfLink.andAffordance(afford(methodOn(WorkloadRestController.class)
                    .deleteWorker(resource.getClusterId(), resource.getId())));

            resource.add(linkTo(methodOn(WorkloadRestController.class)
                    .deleteWorker(resource.getClusterId(), resource.getId()))
                    .withRel(LinkRelations.DELETE_REL));
        }

        resource.add(selfLink);

        return resource;
    }
}
