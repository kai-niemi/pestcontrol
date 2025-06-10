package io.cockroachdb.pestcontrol.api.cluster.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.hateoas.EntityModel;
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
import io.cockroachdb.pestcontrol.operator.ClusterOperator;
import io.cockroachdb.pestcontrol.util.JsonFormatter;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/cluster/agent")
public class AgentController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    @Qualifier("localClusterOperator")
    private ClusterOperator localClusterOperator;

    @Autowired
    private ApplicationProperties applicationProperties;

    @GetMapping("/{clusterId}/node/{nodeId}")
    public HttpEntity<EntityModel<ClusterProperties>> agentForm(
            @PathVariable("clusterId") String clusterId,
            @PathVariable("nodeId") Integer nodeId) {
        Assert.isTrue(nodeId > 0, "nodeId must be > 0");

        ClusterProperties clusterProperties = applicationProperties
                .getClusterPropertiesById(clusterId);

        return ResponseEntity.ok(EntityModel.of(clusterProperties)
                .add(linkTo(methodOn(getClass())
                        .agentForm(clusterId, nodeId))
                        .withSelfRel())
                .add(linkTo(methodOn(getClass())
                        .startNode(clusterId, nodeId, null))
                        .withRel(LinkRelations.AGENT_START_REL))
                .add(linkTo(methodOn(getClass())
                        .stopNode(clusterId, nodeId, null))
                        .withRel(LinkRelations.AGENT_STOP_REL))
        );
    }

    @PostMapping("/{clusterId}/start/{nodeId}")
    public HttpEntity<String> startNode(
            @PathVariable("clusterId") String clusterId,
            @PathVariable("nodeId") Integer nodeId,
            @RequestBody @Valid ClusterProperties clusterProperties) {
        Assert.isTrue(nodeId > 0, "nodeId must be > 0");

        logger.info("Start cluster '%s' node %d\n%s"
                .formatted(clusterId, nodeId, JsonFormatter.toFormattedJSON(clusterProperties)));

        String responseString = localClusterOperator.startNode(clusterProperties, nodeId);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(responseString);
    }

    @PostMapping("/{clusterId}/stop/{nodeId}")
    public HttpEntity<Void> stopNode(
            @PathVariable("clusterId") String clusterId,
            @PathVariable("nodeId") Integer nodeId,
            @RequestBody @Valid ClusterProperties clusterProperties) {
        Assert.isTrue(nodeId > 0, "nodeId must be > 0");

        logger.info("Stop cluster '%s' node %d\n%s"
                .formatted(clusterId, nodeId, JsonFormatter.toFormattedJSON(clusterProperties)));

        localClusterOperator.stopNode(clusterProperties, nodeId);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .build();
    }
}
