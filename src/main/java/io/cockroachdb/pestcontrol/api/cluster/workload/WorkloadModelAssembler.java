package io.cockroachdb.pestcontrol.api.cluster.workload;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import io.cockroachdb.pestcontrol.api.LinkRelations;
import io.cockroachdb.pestcontrol.workload.model.Workload;
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
