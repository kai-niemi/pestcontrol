package io.cockroachdb.pestcontrol.api.cluster.agent;

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
import io.cockroachdb.pestcontrol.model.NodeProperties;

@RestController
@RequestMapping("/api/cluster/agent")
public class AgentController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApplicationProperties applicationProperties;

    @GetMapping(value = "/{clusterId}")
    public HttpEntity<AgentModel> getAgent(
            @PathVariable("clusterId") String clusterId) {
        ClusterProperties clusterProperties = applicationProperties
                .getClusterPropertiesById(clusterId);

        AgentModel resource = new AgentAssembler(clusterId)
                .toModel(new AgentModel());
        resource.setNodes(clusterProperties.getNodes());

        return ResponseEntity.ok(resource);
    }

    @PostMapping("/{clusterId}/start/{nodeId}")
    public HttpEntity<Void> startNode(
            @PathVariable("clusterId") String clusterId,
            @PathVariable("nodeId") Integer nodeId,
            @RequestBody @Valid AgentModel model) {
        Assert.isTrue(nodeId > 0, "nodeId must be > 0");

        NodeProperties node = model.findNodeProperties(nodeId);

        logger.info("Start node: %s".formatted(node));

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .build();
    }

    @PostMapping("/{clusterId}/stop/{nodeId}")
    public HttpEntity<Void> stopNode(
            @PathVariable("clusterId") String clusterId,
            @PathVariable("nodeId") Integer nodeId,
            @RequestBody @Valid AgentModel model) {
        Assert.isTrue(nodeId > 0, "nodeId must be > 0");

        NodeProperties node = model.findNodeProperties(nodeId);

        logger.info("Stop node: %s".formatted(node));

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .build();
    }
}
