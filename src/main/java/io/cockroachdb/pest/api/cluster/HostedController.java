package io.cockroachdb.pest.api.cluster;

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

import io.cockroachdb.pest.api.LinkRelations;
import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.model.ApplicationProperties;
import io.cockroachdb.pest.model.ClusterProperties;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/cluster/hosted")
public class HostedController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    @Qualifier("localClusterOperator")
    private ClusterOperator localClusterOperator;

    @Autowired
    private ApplicationProperties applicationProperties;

    @GetMapping("/{clusterId}")
    public HttpEntity<EntityModel<ClusterProperties>> hostedForm(
            @PathVariable("clusterId") String clusterId) {
        ClusterProperties clusterProperties = applicationProperties
                .getClusterPropertiesById(clusterId);

        return ResponseEntity.ok(EntityModel.of(clusterProperties)
                .add(linkTo(methodOn(getClass())
                        .hostedForm(clusterId))
                        .withSelfRel())
                .add(linkTo(methodOn(getClass())
                        .startNode(clusterId, null, null))
                        .withRel(LinkRelations.NODE_START_REL))
                .add(linkTo(methodOn(getClass())
                        .stopNode(clusterId, null, null))
                        .withRel(LinkRelations.NODE_STOP_REL))
                .add(linkTo(methodOn(getClass())
                        .killNode(clusterId, null, null))
                        .withRel(LinkRelations.NODE_KILL_REL))
                .add(linkTo(methodOn(getClass())
                        .init(clusterId, null, null))
                        .withRel(LinkRelations.NODE_INIT_REL))
                .add(linkTo(methodOn(getClass())
                        .install(clusterId, null, null))
                        .withRel(LinkRelations.NODE_INSTALL_REL))
        );
    }

    @PostMapping("/{clusterId}/start/{nodeId}")
    public HttpEntity<String> startNode(
            @PathVariable("clusterId") String clusterId,
            @PathVariable("nodeId") Integer nodeId,
            @RequestBody @Valid ClusterProperties clusterProperties) {
        Assert.isTrue(nodeId > 0, "nodeId must be > 0");

        logger.info("Start cluster '%s' node %d".formatted(clusterId, nodeId));

        String responseString = localClusterOperator.startNode(clusterProperties, nodeId);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(responseString);
    }

    @PostMapping("/{clusterId}/stop/{nodeId}")
    public HttpEntity<String> stopNode(
            @PathVariable("clusterId") String clusterId,
            @PathVariable("nodeId") Integer nodeId,
            @RequestBody @Valid ClusterProperties clusterProperties) {
        Assert.isTrue(nodeId > 0, "nodeId must be > 0");

        logger.info("Stop cluster '%s' node %d".formatted(clusterId, nodeId));

        String responseString = localClusterOperator.stopNode(clusterProperties, nodeId);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(responseString);
    }

    @PostMapping("/{clusterId}/kill/{nodeId}")
    public HttpEntity<String> killNode(
            @PathVariable("clusterId") String clusterId,
            @PathVariable("nodeId") Integer nodeId,
            @RequestBody @Valid ClusterProperties clusterProperties) {
        Assert.isTrue(nodeId > 0, "nodeId must be > 0");

        logger.info("Kill cluster '%s' node %d".formatted(clusterId, nodeId));

        String responseString = localClusterOperator.killNode(clusterProperties, nodeId);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(responseString);
    }

    @PostMapping("/{clusterId}/init/{nodeId}")
    public HttpEntity<String> init(
            @PathVariable("clusterId") String clusterId,
            @PathVariable("nodeId") Integer nodeId,
            @RequestBody @Valid ClusterProperties clusterProperties) {
        Assert.isTrue(nodeId > 0, "nodeId must be > 0");

        logger.info("Init cluster '%s' via node %d".formatted(clusterId, nodeId));

        String responseString = localClusterOperator.init(clusterProperties, nodeId);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(responseString);
    }

    @PostMapping("/{clusterId}/install/{nodeId}")
    public HttpEntity<String> install(
            @PathVariable("clusterId") String clusterId,
            @PathVariable("nodeId") Integer nodeId,
            @RequestBody @Valid ClusterProperties clusterProperties) {
        Assert.isTrue(nodeId > 0, "nodeId must be > 0");

        logger.info("Install cluster '%s' node %d version %s"
                .formatted(clusterId, nodeId, clusterProperties.getVersion()));

        String responseString = localClusterOperator.install(clusterProperties, nodeId);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(responseString);
    }
}
