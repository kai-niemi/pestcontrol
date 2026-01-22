package io.cockroachdb.pest.cluster.hosted;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.client.Hop;
import org.springframework.http.ResponseEntity;

import io.cockroachdb.pest.cluster.NodeOperator;
import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.model.LinkRelations;
import io.cockroachdb.pest.util.HypermediaClient;
import static io.cockroachdb.pest.model.LinkRelations.CERTS_REL;
import static io.cockroachdb.pest.model.LinkRelations.CLUSTERS_REL;
import static io.cockroachdb.pest.model.LinkRelations.CURIE_NAMESPACE;
import static io.cockroachdb.pest.model.LinkRelations.NODE_INIT_REL;
import static io.cockroachdb.pest.model.LinkRelations.NODE_INSTALL_REL;
import static io.cockroachdb.pest.model.LinkRelations.NODE_KILL_REL;
import static io.cockroachdb.pest.model.LinkRelations.NODE_START_REL;
import static io.cockroachdb.pest.model.LinkRelations.NODE_STATUS_REL;
import static io.cockroachdb.pest.model.LinkRelations.NODE_STOP_REL;
import static io.cockroachdb.pest.model.LinkRelations.NODE_WIPE_REL;
import static io.cockroachdb.pest.model.LinkRelations.OPERATOR_REL;
import static org.springframework.hateoas.mediatype.hal.HalLinkRelation.curied;

public class HostedNodeOperator implements NodeOperator {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Cluster cluster;

    private final HypermediaClient hypermediaClient;

    private final NodeOperator localNodeOperator;

    public HostedNodeOperator(Cluster cluster, HypermediaClient hypermediaClient, NodeOperator localNodeOperator) {
        this.cluster = cluster;
        this.hypermediaClient = hypermediaClient;
        this.localNodeOperator = localNodeOperator;
    }

    private Link operatorLink(int nodeId) {
        Link baseUrl = cluster.getNodeById(nodeId).getServiceLink();
        return hypermediaClient.from(baseUrl)
                .follow(curied(CURIE_NAMESPACE, CLUSTERS_REL).value())
                .follow(Hop.rel(curied(CURIE_NAMESPACE, LinkRelations.OPERATOR_TEMPLATE_REL).value())
                        .withParameter("clusterType", cluster.getClusterType()))
                .follow(curied(CURIE_NAMESPACE, OPERATOR_REL).value())
                .asTemplatedLink();
    }

    private Link clusterLink(Cluster cluster, int nodeId) {
        Link baseUrl = cluster.getNodeById(nodeId).getServiceLink();
        return hypermediaClient.from(baseUrl)
                .follow(curied(CURIE_NAMESPACE, CLUSTERS_REL).value())
                .asTemplatedLink();
    }


    @Override
    public String certs(List<Integer> nodeIds, Map<Integer, List<Path>> keyFiles) throws IOException {
        localNodeOperator.certs(nodeIds, keyFiles);

        nodeIds.forEach(nodeId -> {
            Link clusterLink = clusterLink(cluster, nodeId);

            Link actionLink = hypermediaClient.from(clusterLink)
                    .follow(curied(CURIE_NAMESPACE, CERTS_REL).value())
                    .asTemplatedLink()
                    .expand(Map.of());

            ResponseEntity<String> response = hypermediaClient.upload(actionLink, keyFiles.get(nodeId));
            if (!response.getStatusCode().is2xxSuccessful()) {
                logger.warn("Unexpected HTTP status: {}", response);
            }
        });

        return "";
    }

    @Override
    public String install(Integer nodeId) {
        Link actionLink = hypermediaClient.from(operatorLink(nodeId))
                .follow(curied(CURIE_NAMESPACE, NODE_INSTALL_REL).value())
                .asTemplatedLink()
                .expand(Map.of(
                        "clusterId", cluster.getClusterId(),
                        "nodeId", nodeId));

        ResponseEntity<String> response = hypermediaClient.post(actionLink, cluster, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        return response.getBody();
    }

    @Override
    public String startNode(Integer nodeId) {
        Link base = operatorLink(nodeId);
        Link actionLink = hypermediaClient.from(base)
                .follow(curied(CURIE_NAMESPACE, NODE_START_REL).value())
                .asTemplatedLink()
                .expand(Map.of(
                        "clusterId", cluster.getClusterId(),
                        "nodeId", nodeId));

        ResponseEntity<String> response = hypermediaClient.post(actionLink, cluster, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        return response.getBody();
    }

    @Override
    public String stopNode(Integer nodeId) {
        Link actionLink = hypermediaClient.from(operatorLink(nodeId))
                .follow(curied(CURIE_NAMESPACE, NODE_STOP_REL).value())
                .asTemplatedLink()
                .expand(Map.of(
                        "clusterId", cluster.getClusterId(),
                        "nodeId", nodeId));

        ResponseEntity<String> response = hypermediaClient.post(actionLink, cluster, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        return response.getBody();
    }

    @Override
    public String killNode(Integer nodeId) {
        Link actionLink = hypermediaClient.from(operatorLink(nodeId))
                .follow(curied(CURIE_NAMESPACE, NODE_KILL_REL).value())
                .asTemplatedLink()
                .expand(Map.of(
                        "clusterId", cluster.getClusterId(),
                        "nodeId", nodeId));

        ResponseEntity<String> response = hypermediaClient.post(actionLink, cluster, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        return response.getBody();
    }

    @Override
    public String init(Integer nodeId) {
        Link actionLink = hypermediaClient.from(operatorLink(nodeId))
                .follow(curied(CURIE_NAMESPACE, NODE_INIT_REL).value())
                .asTemplatedLink()
                .expand(Map.of(
                        "clusterId", cluster.getClusterId(),
                        "nodeId", nodeId));

        ResponseEntity<String> response = hypermediaClient.post(actionLink, cluster, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        return response.getBody();
    }

    @Override
    public String wipe(Integer nodeId, boolean all) {
        Link actionLink = hypermediaClient.from(operatorLink(nodeId))
                .follow(curied(CURIE_NAMESPACE, NODE_WIPE_REL).value())
                .asTemplatedLink()
                .expand(Map.of(
                        "clusterId", cluster.getClusterId(),
                        "nodeId", nodeId,
                        "all", all));

        ResponseEntity<String> response = hypermediaClient.post(actionLink, cluster, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        return response.getBody();
    }

    @Override
    public String sqlNode(Integer nodeId) throws IOException {
        return localNodeOperator.sqlNode(nodeId);
    }

    @Override
    public String statusNode(Integer nodeId) {
        Link actionLink = hypermediaClient.from(operatorLink(nodeId))
                .follow(curied(CURIE_NAMESPACE, NODE_STATUS_REL).value())
                .asTemplatedLink()
                .expand(Map.of(
                        "clusterId", cluster.getClusterId(),
                        "nodeId", nodeId));

        ResponseEntity<String> response = hypermediaClient.post(actionLink, cluster, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        return response.getBody();
    }
}
