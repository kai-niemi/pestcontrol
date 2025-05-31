package io.cockroachdb.pestcontrol.api.cluster.machine;

import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
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

@RestController
@RequestMapping("/api/cluster/machine")
public class MachineController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApplicationProperties applicationProperties;

    @GetMapping(value = "/{clusterId}")
    public HttpEntity<MachineModel> getClusterIndex(
            @PathVariable("clusterId") String clusterId) {
        MachineModel model = MachineModel.fromId(clusterId);

        ClusterProperties clusterProperties = applicationProperties
                .getClusterPropertiesById(clusterId);
        if (EnumSet.of(ClusterType.remote_insecure, ClusterType.remote_secure)
                .contains(clusterProperties.getClusterType())) {
            model.setMachines(clusterProperties.getMachines());
        }

        return ResponseEntity.ok(new MachineModelAssembler().toModel(model));
    }

    @PostMapping("/start/{nodeId}")
    public HttpEntity<Void> startMachine(
            @PathVariable("nodeId") Integer nodeId,
            @RequestBody @Valid MachineModel form) {
        Assert.isTrue(nodeId > 0, "nodeId must be > 0");

        MachineProperties agent = form.getMachines()
                .stream()
                .skip(nodeId - 1)
                .findFirst()
                .orElseThrow();

        logger.info("Start: %s".formatted(agent));

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .build();
    }

    @PostMapping("/stop/{nodeId}")
    public HttpEntity<Void> stopMachine(
            @PathVariable("nodeId") Integer nodeId,
            @RequestBody @Valid MachineModel form) {
        Assert.isTrue(nodeId > 0, "nodeId must be > 0");

        MachineProperties agent = form.getMachines()
                .stream()
                .skip(nodeId - 1)
                .findFirst()
                .orElseThrow();

        logger.info("Stop: %s".formatted(agent));

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .build();
    }
}
