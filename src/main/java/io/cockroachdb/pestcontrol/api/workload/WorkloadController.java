package io.cockroachdb.pestcontrol.api.workload;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Links;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.cockroachdb.pestcontrol.api.LinkRelations;
import io.cockroachdb.pestcontrol.model.ApplicationProperties;
import io.cockroachdb.pestcontrol.workload.WorkloadManager;
import io.cockroachdb.pestcontrol.workload.model.Workload;
import io.cockroachdb.pestcontrol.workload.profile.WorkloadType;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/workload")
public class WorkloadController {
    @Autowired
    private WorkloadManager workloadManager;

    @Autowired
    private WorkloadModelAssembler workerModelAssembler;

    @Autowired
    private ApplicationProperties applicationProperties;

    @GetMapping("/{clusterId}/workers")
    public ResponseEntity<CollectionModel<Workload>> getWorkers(
            @PathVariable("clusterId") String clusterId) {
        CollectionModel<Workload> collectionModel = workerModelAssembler
                .toCollectionModel(workloadManager.getWorkloads(clusterId));

        collectionModel.add(linkTo(methodOn(WorkloadController.class)
                .getWorkerForm(clusterId))
                .withRel(LinkRelations.FORM_REL));

        Links newLinks = collectionModel.getLinks().merge(Links.MergeMode.REPLACE_BY_REL,
                linkTo(methodOn(WorkloadController.class).getWorkers(clusterId))
                        .withSelfRel()
                        .andAffordance(afford(methodOn(WorkloadController.class)
                                .newWorker(clusterId, null))));

        return ResponseEntity.ok(CollectionModel.of(collectionModel.getContent(), newLinks));
    }

    @GetMapping(value = "/{clusterId}/workers/{id}")
    public HttpEntity<Workload> getWorker(
            @PathVariable("clusterId") String clusterId,
            @PathVariable("id") Integer id) {
        return ResponseEntity.ok(workerModelAssembler.toModel(workloadManager.findById(clusterId, id)));
    }

    @PutMapping(value = "/{clusterId}/workers/{id}/cancel")
    public HttpEntity<Workload> cancelWorker(
            @PathVariable("clusterId") String clusterId,
            @PathVariable("id") Integer id) {
        Workload workload = workloadManager.findById(clusterId, id);
        if (workload.cancel()) {
            return ResponseEntity.ok(workerModelAssembler.toModel(workload));
        } else {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(workerModelAssembler.toModel(workload));
        }
    }

    @DeleteMapping(value = "/{clusterId}/workers/{id}/delete")
    public HttpEntity<Void> deleteWorker(
            @PathVariable("clusterId") String clusterId,
            @PathVariable("id") Integer id) {
        try {
            workloadManager.deleteById(clusterId, id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping(value = "/{clusterId}/workers/form")
    public HttpEntity<WorkloadForm> getWorkerForm(@PathVariable("clusterId") String clusterId) {
        WorkloadForm form = new WorkloadForm();
        form.setWorkloadType(WorkloadType.random_wait);
        form.setDuration("00:15");

        return ResponseEntity.ok(form
                .add(linkTo(methodOn(WorkloadController.class)
                        .getWorkerForm(clusterId))
                        .withSelfRel()
                        .andAffordance(afford(methodOn(WorkloadController.class)
                                .newWorker(clusterId, null)))
                ));
    }

    @PostMapping("/{clusterId}/workers")
    public HttpEntity<CollectionModel<Workload>> newWorker(
            @PathVariable("clusterId") String clusterId,
            @RequestBody WorkloadForm form) {

        final LocalTime time = LocalTime.parse(form.getDuration(), DateTimeFormatter.ofPattern("HH:mm"));
        final Duration duration = Duration.ofHours(time.getHour()).plusMinutes(time.getMinute());
        final DataSource dataSource = applicationProperties.getDataSource(clusterId);

        List<Workload> entities = new ArrayList<>();

        IntStream.rangeClosed(1, form.getCount())
                .forEach(value -> {
                    final Runnable task = form.getWorkloadType().createTask(dataSource);
                    workloadManager.submitWorkload(clusterId, duration, task,
                            form.getWorkloadType().getDisplayValue());
                });

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(workerModelAssembler.toCollectionModel(entities));
    }
}
