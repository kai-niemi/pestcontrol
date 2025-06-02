package io.cockroachdb.pestcontrol.api.cluster.network;

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

import io.cockroachdb.pestcontrol.api.LinkRelations;
import io.cockroachdb.pestcontrol.model.ApplicationProperties;
import io.cockroachdb.pestcontrol.model.ClusterProperties;
import io.cockroachdb.pestcontrol.model.NodeProperties;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/cluster/network")
public class NetworkController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApplicationProperties applicationProperties;

    @GetMapping(value = "/{clusterId}")
    public HttpEntity<NetworkModel> getNetwork(
            @PathVariable("clusterId") String clusterId) {

        ClusterProperties clusterProperties = applicationProperties
                .getClusterPropertiesById(clusterId);

        NetworkModel resource = new NetworkModel();
        resource.setNodes(clusterProperties.getNodes());
        resource.add(linkTo(methodOn(NetworkController.class)
                .getNetwork(clusterId))
                .withSelfRel());
        resource.add(linkTo(methodOn(NetworkController.class)
                .startMachine(clusterId, null, null))
                .withRel(LinkRelations.NODE_START_REL));
        resource.add(linkTo(methodOn(NetworkController.class)
                .stopMachine(clusterId, null, null))
                .withRel(LinkRelations.NODE_STOP_REL));

        return ResponseEntity.ok(resource);
    }

    @PostMapping("/{clusterId}/start/{nodeId}")
    public HttpEntity<Void> startMachine(
            @PathVariable("clusterId") String clusterId,
            @PathVariable("nodeId") Integer nodeId,
            @RequestBody @Valid NetworkModel model) {
        Assert.isTrue(nodeId > 0, "nodeId must be > 0");

        NodeProperties node = model.findNodeProperties(nodeId);

        logger.info("Start: %s".formatted(node));

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .build();
    }

    @PostMapping("/{clusterId}/stop/{nodeId}")
    public HttpEntity<Void> stopMachine(
            @PathVariable("clusterId") String clusterId,
            @PathVariable("nodeId") Integer nodeId,
            @RequestBody @Valid NetworkModel model) {
        Assert.isTrue(nodeId > 0, "nodeId must be > 0");

        NodeProperties node = model.findNodeProperties(nodeId);

        logger.info("Stop: %s".formatted(node));

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .build();
    }
}
