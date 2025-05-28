package io.cockroachdb.pestcontrol.api.machine;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Links;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import io.cockroachdb.pestcontrol.model.ApplicationProperties;
import io.cockroachdb.pestcontrol.model.ClusterProperties;
import io.cockroachdb.pestcontrol.model.ClusterType;
import io.cockroachdb.pestcontrol.model.MachineProperties;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/machine")
public class MachinesController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApplicationProperties applicationProperties;

    @GetMapping
    public ResponseEntity<CollectionModel<MachinesForm>> index() {
        List<MachinesForm> forms = new ArrayList<>();

        applicationProperties.getClusterIds().forEach(clusterId -> {
            ClusterProperties clusterProperties = applicationProperties.getClusterPropertiesById(clusterId);
            if (EnumSet.of(ClusterType.remote_insecure, ClusterType.remote_secure)
                    .contains(clusterProperties.getClusterType())) {
                MachinesForm form = new MachinesForm();
                form.setClusterId(clusterId);
                form.setMachines(clusterProperties.getMachines());
                forms.add(new MachineFormAssembler().toModel(form));
            }
        });

        CollectionModel<MachinesForm> collectionModel = CollectionModel.of(forms);

        Links links = collectionModel.getLinks().merge(Links.MergeMode.REPLACE_BY_REL,
                linkTo(methodOn(getClass())
                        .index())
                        .withSelfRel());

        return ResponseEntity.ok(CollectionModel.of(collectionModel.getContent(), links));
    }

    @GetMapping(value = "/{clusterId}")
    public HttpEntity<MachinesForm> getForm(
            @PathVariable("clusterId") String clusterId) {
        ClusterProperties clusterProperties = applicationProperties.getClusterPropertiesById(clusterId);
        if (EnumSet.of(ClusterType.remote_insecure, ClusterType.remote_secure)
                .contains(clusterProperties.getClusterType())) {
            MachinesForm form = new MachinesForm();
            form.setClusterId(clusterId);
            form.setMachines(clusterProperties.getMachines());
            return ResponseEntity.ok(new MachineFormAssembler().toModel(form));
        }
        return ResponseEntity.unprocessableEntity().build();
    }

    @PostMapping("/start")
    public HttpEntity<Void> startMachine(
            @RequestBody @Valid MachinesForm form) {
        MachineProperties agent = form.getMachines()
                .stream()
                .skip(form.getNodeId() - 1)
                .findFirst()
                .orElseThrow();

        logger.info("Start: %s".formatted(agent));

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .build();
    }

    @PostMapping("/stop")
    public HttpEntity<Void> stopMachine(
            @RequestBody @Valid MachinesForm form) {
        MachineProperties agent = form.getMachines()
                .stream()
                .skip(form.getNodeId() - 1)
                .findFirst()
                .orElseThrow();

        logger.info("Stop: %s".formatted(agent));

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .build();
    }
}
