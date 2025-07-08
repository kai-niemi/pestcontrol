package io.cockroachdb.pest.cluster;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.client.Hop;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.cockroachdb.pest.model.ClusterProperties;
import io.cockroachdb.pest.model.ClusterType;
import io.cockroachdb.pest.shell.client.HypermediaClient;
import static io.cockroachdb.pest.api.LinkRelations.CERTS_REL;
import static io.cockroachdb.pest.api.LinkRelations.CLUSTER_TEMPLATE_REL;
import static io.cockroachdb.pest.api.LinkRelations.NODE_PROXY_START_REL;
import static io.cockroachdb.pest.api.LinkRelations.NODE_WIPE_REL;
import static io.cockroachdb.pest.api.LinkRelations.OPERATOR_REL;
import static io.cockroachdb.pest.api.LinkRelations.NODE_INIT_REL;
import static io.cockroachdb.pest.api.LinkRelations.NODE_INSTALL_REL;
import static io.cockroachdb.pest.api.LinkRelations.NODE_KILL_REL;
import static io.cockroachdb.pest.api.LinkRelations.NODE_START_REL;
import static io.cockroachdb.pest.api.LinkRelations.NODE_STOP_REL;
import static io.cockroachdb.pest.api.LinkRelations.CLUSTERS_REL;
import static io.cockroachdb.pest.api.LinkRelations.CURIE_NAMESPACE;
import static org.springframework.hateoas.mediatype.hal.HalLinkRelation.curied;

@Component
public class HostedClusterOperator implements ClusterOperator {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private HypermediaClient hypermediaClient;

    @Autowired
    @Qualifier("localClusterOperator")
    private ClusterOperator localClusterOperator;

    @Override
    public boolean supports(ClusterType clusterType) {
        return EnumSet.of(ClusterType.hosted_insecure, ClusterType.hosted_secure)
                .contains(clusterType);
    }

    private Link clusterLink(ClusterProperties clusterProperties, int nodeId) {
        Link baseUrl = clusterProperties.findNodePropertiesById(nodeId).getBaseUrl();
        return hypermediaClient.from(baseUrl)
                .follow(curied(CURIE_NAMESPACE, CLUSTERS_REL).value())
                .asTemplatedLink();
    }

    private Link nodeOperatorLink(ClusterProperties clusterProperties, int nodeId) {
        Link baseUrl = clusterProperties.findNodePropertiesById(nodeId).getBaseUrl();
        return hypermediaClient.from(baseUrl)
                .follow(curied(CURIE_NAMESPACE, CLUSTERS_REL).value())
                .follow(Hop.rel(curied(CURIE_NAMESPACE, CLUSTER_TEMPLATE_REL).value())
                        .withParameter("clusterId", clusterProperties.getClusterId()))
                .follow(Hop.rel(curied(CURIE_NAMESPACE, OPERATOR_REL).value()))
                .asTemplatedLink();
    }

    @Override
    public String certs(ClusterProperties clusterProperties, List<Integer> nodeIds, Map<Integer, List<Path>> keyFiles) {
        localClusterOperator.certs(clusterProperties, nodeIds, keyFiles);

        nodeIds.forEach(nodeId -> {
            Link clusterLink = clusterLink(clusterProperties, nodeId);

            Link actionLink = hypermediaClient.from(clusterLink)
                    .follow(curied(CURIE_NAMESPACE, CERTS_REL).value())
                    .asTemplatedLink()
                    .expand(Map.of());

            ResponseEntity<String> response = hypermediaClient.upload(actionLink, keyFiles.get(nodeId));
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("HTTP status: {}", response);
            } else {
                logger.warn("Unexpected HTTP status: {}", response);
            }
        });

        return "";
    }

    @Override
    public String install(ClusterProperties clusterProperties, Integer nodeId) {
        Link operatorLink = nodeOperatorLink(clusterProperties, nodeId);
        Link actionLink = hypermediaClient.from(operatorLink)
                .follow(curied(CURIE_NAMESPACE, NODE_INSTALL_REL).value())
                .asTemplatedLink()
                .expand(Map.of(
                        "clusterId", clusterProperties.getClusterId(),
                        "nodeId", nodeId));

        ResponseEntity<String> response = hypermediaClient.post(actionLink, clusterProperties, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("HTTP status: {}", response);
        } else {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        return response.getBody();
    }

    @Override
    public String startNode(ClusterProperties clusterProperties, Integer nodeId) {
        Link operatorLink = nodeOperatorLink(clusterProperties, nodeId);
        Link actionLink = hypermediaClient.from(operatorLink)
                .follow(curied(CURIE_NAMESPACE, NODE_START_REL).value())
                .asTemplatedLink()
                .expand(Map.of(
                        "clusterId", clusterProperties.getClusterId(),
                        "nodeId", nodeId));

        ResponseEntity<String> response = hypermediaClient.post(actionLink, clusterProperties, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("HTTP status: {}", response);
        } else {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        return response.getBody();
    }

    @Override
    public String stopNode(ClusterProperties clusterProperties, Integer nodeId) {
        Link operatorLink = nodeOperatorLink(clusterProperties, nodeId);
        Link actionLink = hypermediaClient.from(operatorLink)
                .follow(curied(CURIE_NAMESPACE, NODE_STOP_REL).value())
                .asTemplatedLink()
                .expand(Map.of(
                        "clusterId", clusterProperties.getClusterId(),
                        "nodeId", nodeId));

        ResponseEntity<String> response = hypermediaClient.post(actionLink, clusterProperties, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("HTTP status: {}", response);
        } else {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        return response.getBody();
    }

    @Override
    public String killNode(ClusterProperties clusterProperties, Integer nodeId) {
        Link operatorLink = nodeOperatorLink(clusterProperties, nodeId);
        Link actionLink = hypermediaClient.from(operatorLink)
                .follow(curied(CURIE_NAMESPACE, NODE_KILL_REL).value())
                .asTemplatedLink()
                .expand(Map.of(
                        "clusterId", clusterProperties.getClusterId(),
                        "nodeId", nodeId));

        ResponseEntity<String> response = hypermediaClient.post(actionLink, clusterProperties, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("HTTP status: {}", response);
        } else {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        return response.getBody();
    }

    @Override
    public String init(ClusterProperties clusterProperties, Integer nodeId) {
        Link operatorLink = nodeOperatorLink(clusterProperties, nodeId);
        Link actionLink = hypermediaClient.from(operatorLink)
                .follow(curied(CURIE_NAMESPACE, NODE_INIT_REL).value())
                .asTemplatedLink()
                .expand(Map.of(
                        "clusterId", clusterProperties.getClusterId(),
                        "nodeId", nodeId));

        ResponseEntity<String> response = hypermediaClient.post(actionLink, clusterProperties, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("HTTP status: {}", response);
        } else {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        return response.getBody();
    }

    @Override
    public String wipe(ClusterProperties clusterProperties, Integer nodeId) {
        Link operatorLink = nodeOperatorLink(clusterProperties, nodeId);
        Link actionLink = hypermediaClient.from(operatorLink)
                .follow(curied(CURIE_NAMESPACE, NODE_WIPE_REL).value())
                .asTemplatedLink()
                .expand(Map.of(
                        "clusterId", clusterProperties.getClusterId(),
                        "nodeId", nodeId));

        ResponseEntity<String> response = hypermediaClient.post(actionLink, clusterProperties, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("HTTP status: {}", response);
        } else {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        return response.getBody();
    }

    @Override
    public String startProxyClient(ClusterProperties clusterProperties, Integer nodeId) {
        Link operatorLink = nodeOperatorLink(clusterProperties, nodeId);
        Link actionLink = hypermediaClient.from(operatorLink)
                .follow(curied(CURIE_NAMESPACE, NODE_PROXY_START_REL).value())
                .asTemplatedLink()
                .expand(Map.of(
                        "clusterId", clusterProperties.getClusterId(),
                        "nodeId", nodeId));

        ResponseEntity<String> response = hypermediaClient.post(actionLink, clusterProperties, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("HTTP status: {}", response);
        } else {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        return response.getBody();
    }

    @Override
    public String sqlNode(ClusterProperties clusterProperties, Integer nodeId) {
        return localClusterOperator.sqlNode(clusterProperties, nodeId);
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
