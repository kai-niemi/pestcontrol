package io.cockroachdb.pest.cluster;

import java.util.EnumSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.client.Hop;
import org.springframework.hateoas.client.Traverson;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.cockroachdb.pest.model.ClusterProperties;
import io.cockroachdb.pest.model.ClusterType;
import io.cockroachdb.pest.shell.client.HypermediaClient;
import static io.cockroachdb.pest.api.LinkRelations.HOSTED_REL;
import static io.cockroachdb.pest.api.LinkRelations.NODE_INIT_REL;
import static io.cockroachdb.pest.api.LinkRelations.NODE_INSTALL_REL;
import static io.cockroachdb.pest.api.LinkRelations.NODE_KILL_REL;
import static io.cockroachdb.pest.api.LinkRelations.NODE_START_REL;
import static io.cockroachdb.pest.api.LinkRelations.NODE_STOP_REL;
import static io.cockroachdb.pest.api.LinkRelations.CLUSTER_COLL_REL;
import static io.cockroachdb.pest.api.LinkRelations.CLUSTER_REL;
import static io.cockroachdb.pest.api.LinkRelations.CURIE_NAMESPACE;
import static org.springframework.hateoas.mediatype.hal.HalLinkRelation.curied;

@Component
public class HostedClusterOperator implements ClusterOperator {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private HypermediaClient hypermediaClient;

    @Override
    public boolean supports(ClusterType clusterType) {
        return EnumSet.of(ClusterType.hosted_insecure, ClusterType.hosted_secure)
                .contains(clusterType);
    }

    private Traverson.TraversalBuilder traversalBuilder(ClusterProperties clusterProperties,
                                                        int nodeId) {
        Link baseUrl = clusterProperties.findNodePropertiesById(nodeId).getBaseUrl();
        return hypermediaClient.from(baseUrl)
                .follow(curied(CURIE_NAMESPACE, CLUSTER_COLL_REL).value())
                .follow(Hop.rel(curied(CURIE_NAMESPACE, CLUSTER_REL).value())
                        .withParameter("clusterId", clusterProperties.getClusterId()))
                .follow(Hop.rel(curied(CURIE_NAMESPACE, HOSTED_REL).value())
                        .withParameter("clusterId", clusterProperties.getClusterId()));
    }

    @Override
    public String install(ClusterProperties clusterProperties, Integer nodeId) {
        Link link = traversalBuilder(clusterProperties, nodeId)
                .follow(curied(CURIE_NAMESPACE, NODE_INSTALL_REL).value())
                .asTemplatedLink()
                .expand(Map.of(
                        "clusterId", clusterProperties.getClusterId(),
                        "nodeId", nodeId));

        ResponseEntity<String> response = hypermediaClient.post(link, clusterProperties, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("HTTP status: {}", response);
        } else {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        return response.getBody();
    }

    @Override
    public String startNode(ClusterProperties clusterProperties, Integer nodeId) {
        Link link = traversalBuilder(clusterProperties, nodeId)
                .follow(curied(CURIE_NAMESPACE, NODE_START_REL).value())
                .asTemplatedLink()
                .expand(Map.of(
                        "clusterId", clusterProperties.getClusterId(),
                        "nodeId", nodeId));

        ResponseEntity<String> response = hypermediaClient.post(link, clusterProperties, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("HTTP status: {}", response);
        } else {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        return response.getBody();
    }

    @Override
    public String stopNode(ClusterProperties clusterProperties, Integer nodeId) {
        Link link = traversalBuilder(clusterProperties, nodeId)
                .follow(curied(CURIE_NAMESPACE, NODE_STOP_REL).value())
                .asTemplatedLink()
                .expand(Map.of(
                        "clusterId", clusterProperties.getClusterId(),
                        "nodeId", nodeId));

        ResponseEntity<String> response = hypermediaClient.post(link, clusterProperties, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("HTTP status: {}", response);
        } else {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        return response.getBody();
    }

    @Override
    public String killNode(ClusterProperties clusterProperties, Integer nodeId) {
        Link link = traversalBuilder(clusterProperties, nodeId)
                .follow(curied(CURIE_NAMESPACE, NODE_KILL_REL).value())
                .asTemplatedLink()
                .expand(Map.of(
                        "clusterId", clusterProperties.getClusterId(),
                        "nodeId", nodeId));

        ResponseEntity<String> response = hypermediaClient.post(link, clusterProperties, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("HTTP status: {}", response);
        } else {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        return response.getBody();
    }

    @Override
    public String init(ClusterProperties clusterProperties, Integer nodeId) {
        Link link = traversalBuilder(clusterProperties, nodeId)
                .follow(curied(CURIE_NAMESPACE, NODE_INIT_REL).value())
                .asTemplatedLink()
                .expand(Map.of(
                        "clusterId", clusterProperties.getClusterId(),
                        "nodeId", nodeId));

        ResponseEntity<String> response = hypermediaClient.post(link, clusterProperties, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("HTTP status: {}", response);
        } else {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        return response.getBody();
    }

    @Override
    public String disruptNode(ClusterProperties clusterProperties, Integer nodeId) {
        return killNode(clusterProperties, nodeId);
    }

    @Override
    public String recoverNode(ClusterProperties clusterProperties, Integer nodeId) {
        return startNode(clusterProperties, nodeId);
    }

    @Override
    public String disruptNodes(ClusterProperties cluster, String locality) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String recoverNodes(ClusterProperties cluster, String locality) {
        throw new UnsupportedOperationException();
    }
}
