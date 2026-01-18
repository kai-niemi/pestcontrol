package io.cockroachdb.pest.web.api.cluster;

import java.io.IOException;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import io.cockroachdb.pest.cluster.local.LocalClusterOperator;
import io.cockroachdb.pest.domain.Cluster;
import io.cockroachdb.pest.web.LinkRelations;
import io.cockroachdb.pest.web.api.MessageModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/cluster/local")
public class LocalOperatorController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private LocalClusterOperator localClusterOperator;

    @GetMapping
    public HttpEntity<MessageModel> index()  throws IOException {
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
                        .statusNode(null, null))
                        .withRel(LinkRelations.NODE_STATUS_REL))
                .add(linkTo(methodOn(getClass())
                        .killNode(null, null))
                        .withRel(LinkRelations.NODE_KILL_REL))
                .add(linkTo(methodOn(getClass())
                        .init(null, null))
                        .withRel(LinkRelations.NODE_INIT_REL))
                .add(linkTo(methodOn(getClass())
                        .install(null, null))
                        .withRel(LinkRelations.NODE_INSTALL_REL))
                .add(linkTo(methodOn(getClass())
                        .wipe(null, null, null))
                        .withRel(LinkRelations.NODE_WIPE_REL))
                .add(linkTo(methodOn(getClass())
                        .genHAProxyCfg(null, null))
                        .withRel(LinkRelations.NODE_GEN_HAPROXY_REL))
                .add(linkTo(methodOn(getClass())
                        .startHAProxy(null, null))
                        .withRel(LinkRelations.NODE_START_HAPROXY_REL))
                .add(linkTo(methodOn(getClass())
                        .stopHAProxy(null, null))
                        .withRel(LinkRelations.NODE_STOP_HAPROXY_REL))
        );
    }

    @PostMapping("/{nodeId}/start")
    public HttpEntity<String> startNode(
            @PathVariable("nodeId") Integer nodeId,
            @RequestBody @Valid Cluster cluster)  throws IOException {
        Assert.isTrue(nodeId > 0, "nodeId must be > 0");

        logger.info("Start cluster '%s' node %d".formatted(cluster.getClusterId(), nodeId));

        String responseString = localClusterOperator
                .nodeOperator(cluster)
                .startNode(nodeId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseString);
    }

    @PostMapping("/{nodeId}/stop")
    public HttpEntity<String> stopNode(
            @PathVariable("nodeId") Integer nodeId,
            @RequestBody @Valid Cluster cluster)  throws IOException{
        Assert.isTrue(nodeId > 0, "nodeId must be > 0");

        logger.info("Stop cluster '%s' node %d".formatted(cluster.getClusterId(), nodeId));

        String responseString = localClusterOperator
                .nodeOperator(cluster)
                .stopNode(nodeId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseString);
    }

    @PostMapping("/{nodeId}/status")
    public HttpEntity<String> statusNode(
            @PathVariable("nodeId") Integer nodeId,
            @RequestBody @Valid Cluster cluster) throws IOException {
        Assert.isTrue(nodeId > 0, "nodeId must be > 0");

        logger.info("Query cluster '%s' node %d status".formatted(cluster.getClusterId(), nodeId));

        String responseString = localClusterOperator
                .nodeOperator(cluster)
                .statusNode(nodeId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseString);
    }

    @PostMapping("/{nodeId}/kill")
    public HttpEntity<String> killNode(
            @PathVariable("nodeId") Integer nodeId,
            @RequestBody @Valid Cluster cluster)  throws IOException{
        Assert.isTrue(nodeId > 0, "nodeId must be > 0");

        logger.info("Kill cluster '%s' node %d".formatted(cluster.getClusterId(), nodeId));

        String responseString = localClusterOperator
                .nodeOperator(cluster)
                .killNode(nodeId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseString);
    }

    @PostMapping("/{nodeId}/init")
    public HttpEntity<String> init(
            @PathVariable("nodeId") Integer nodeId,
            @RequestBody @Valid Cluster cluster)  throws IOException{
        Assert.isTrue(nodeId > 0, "nodeId must be > 0");

        logger.info("Init cluster '%s' via node %d".formatted(cluster.getClusterId(), nodeId));

        String responseString = localClusterOperator
                .nodeOperator(cluster)
                .init(nodeId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseString);
    }

    @PostMapping("/{nodeId}/install")
    public HttpEntity<String> install(
            @PathVariable("nodeId") Integer nodeId,
            @RequestBody @Valid Cluster cluster)  throws IOException{
        Assert.isTrue(nodeId > 0, "nodeId must be > 0");

        logger.info("Install cluster '%s' node %d".formatted(cluster.getClusterId(), nodeId));

        String responseString = localClusterOperator
                .nodeOperator(cluster)
                .install(nodeId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseString);
    }

    @PostMapping("/{nodeId}/wipe")
    public HttpEntity<String> wipe(
            @PathVariable("nodeId") Integer nodeId,
            @RequestBody @Valid Cluster cluster,
            @RequestParam(value = "all", required = false, defaultValue = "false") Boolean all) throws IOException {
        Assert.isTrue(nodeId > 0, "nodeId must be > 0");

        logger.info("Wipe cluster '%s' node %d".formatted(cluster.getClusterId(), nodeId));

        String responseString = localClusterOperator
                .nodeOperator(cluster)
                .wipe(nodeId, all);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseString);
    }

    @PostMapping("/{nodeId}/gen-haproxy")
    public HttpEntity<String> genHAProxyCfg(
            @PathVariable("nodeId") Integer nodeId,
            @RequestBody @Valid Cluster cluster)  throws IOException{
        Assert.isTrue(nodeId > 0, "nodeId must be > 0");

        logger.info("Generate HAProxy config for cluster '%s' node %d".formatted(cluster.getClusterId(), nodeId));

        String responseString = localClusterOperator
                .proxyOperator(cluster)
                .genHAProxyCfg(nodeId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseString);
    }

    @PostMapping("/{nodeId}/start-haproxy")
    public HttpEntity<String> startHAProxy(
            @PathVariable("nodeId") Integer nodeId,
            @RequestBody @Valid Cluster cluster)  throws IOException{
        Assert.isTrue(nodeId > 0, "nodeId must be > 0");

        logger.info("Start HAProxy for cluster '%s' node %d".formatted(cluster.getClusterId(), nodeId));

        String responseString = localClusterOperator
                .proxyOperator(cluster)
                .startHAProxy(nodeId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseString);
    }

    @PostMapping("/{nodeId}/stop-haproxy")
    public HttpEntity<String> stopHAProxy(
            @PathVariable("nodeId") Integer nodeId,
            @RequestBody @Valid Cluster cluster)  throws IOException{
        Assert.isTrue(nodeId > 0, "nodeId must be > 0");

        logger.info("Stop HAProxy for cluster '%s' node %d".formatted(cluster.getClusterId(), nodeId));

        String responseString = localClusterOperator
                .proxyOperator(cluster)
                .stopHAProxy(nodeId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseString);
    }
}
