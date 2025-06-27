package io.cockroachdb.pest.api.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import io.cockroachdb.pest.api.MessageModel;
import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.model.ClusterProperties;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/cluster/local")
public class LocalController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    @Qualifier("localClusterOperator")
    private ClusterOperator clusterOperator;

    @GetMapping
    public HttpEntity<MessageModel> index() {
        return ResponseEntity.ok(MessageModel.from("Local cluster operator")
                .add(linkTo(methodOn(getClass())
                        .index())
                        .withSelfRel())
                .add(linkTo(methodOn(getClass())
                        .startNode(null, null))
                        .withRel(LinkRelations.NODE_START_REL))
                .add(linkTo(methodOn(getClass())
                        .stopNode(null, null))
                        .withRel(LinkRelations.NODE_STOP_REL))
                .add(linkTo(methodOn(getClass())
                        .killNode(null, null))
                        .withRel(LinkRelations.NODE_KILL_REL))
                .add(linkTo(methodOn(getClass())
                        .init(null, null))
                        .withRel(LinkRelations.NODE_INIT_REL))
                .add(linkTo(methodOn(getClass())
                        .install(null, null))
                        .withRel(LinkRelations.NODE_INSTALL_REL))
        );
    }

    @PostMapping("/{nodeId}/start")
    public HttpEntity<String> startNode(
            @PathVariable("nodeId") Integer nodeId,
            @RequestBody @Valid ClusterProperties clusterProperties) {
        Assert.isTrue(nodeId > 0, "nodeId must be > 0");

        logger.info("Start cluster '%s' node %d".formatted(clusterProperties.getClusterId(), nodeId));

        String responseString = clusterOperator.startNode(clusterProperties, nodeId);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(responseString);
    }

    @PostMapping("/{nodeId}/stop")
    public HttpEntity<String> stopNode(
            @PathVariable("nodeId") Integer nodeId,
            @RequestBody @Valid ClusterProperties clusterProperties) {
        Assert.isTrue(nodeId > 0, "nodeId must be > 0");

        logger.info("Stop cluster '%s' node %d".formatted(clusterProperties.getClusterId(), nodeId));

        String responseString = clusterOperator.stopNode(clusterProperties, nodeId);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(responseString);
    }

    @PostMapping("/{nodeId}/kill")
    public HttpEntity<String> killNode(
            @PathVariable("nodeId") Integer nodeId,
            @RequestBody @Valid ClusterProperties clusterProperties) {
        Assert.isTrue(nodeId > 0, "nodeId must be > 0");

        logger.info("Kill cluster '%s' node %d".formatted(clusterProperties.getClusterId(), nodeId));

        String responseString = clusterOperator.killNode(clusterProperties, nodeId);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(responseString);
    }

    @PostMapping("/{nodeId}/init")
    public HttpEntity<String> init(
            @PathVariable("nodeId") Integer nodeId,
            @RequestBody @Valid ClusterProperties clusterProperties) {
        Assert.isTrue(nodeId > 0, "nodeId must be > 0");

        logger.info("Init cluster '%s' via node %d".formatted(clusterProperties.getClusterId(), nodeId));

        String responseString = clusterOperator.init(clusterProperties, nodeId);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(responseString);
    }

    @PostMapping("/{nodeId}/install")
    public HttpEntity<String> install(
            @PathVariable("nodeId") Integer nodeId,
            @RequestBody @Valid ClusterProperties clusterProperties) {
        Assert.isTrue(nodeId > 0, "nodeId must be > 0");

        logger.info("Install cluster '%s' node %d version %s"
                .formatted(clusterProperties.getClusterId(), nodeId, clusterProperties.getVersion()));

        String responseString = clusterOperator.install(clusterProperties, nodeId);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(responseString);
    }

}
