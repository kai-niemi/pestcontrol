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

import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.model.ClusterType;
import io.cockroachdb.pest.util.HypermediaClient;
import io.cockroachdb.pest.web.LinkRelations;
import static io.cockroachdb.pest.web.LinkRelations.CERTS_REL;
import static io.cockroachdb.pest.web.LinkRelations.CLUSTERS_REL;
import static io.cockroachdb.pest.web.LinkRelations.CURIE_NAMESPACE;
import static io.cockroachdb.pest.web.LinkRelations.NODE_GEN_HAPROXY_REL;
import static io.cockroachdb.pest.web.LinkRelations.NODE_INIT_REL;
import static io.cockroachdb.pest.web.LinkRelations.NODE_INSTALL_REL;
import static io.cockroachdb.pest.web.LinkRelations.NODE_KILL_REL;
import static io.cockroachdb.pest.web.LinkRelations.NODE_START_HAPROXY_REL;
import static io.cockroachdb.pest.web.LinkRelations.NODE_START_REL;
import static io.cockroachdb.pest.web.LinkRelations.NODE_STATUS_REL;
import static io.cockroachdb.pest.web.LinkRelations.NODE_STOP_HAPROXY_REL;
import static io.cockroachdb.pest.web.LinkRelations.NODE_STOP_REL;
import static io.cockroachdb.pest.web.LinkRelations.NODE_WIPE_REL;
import static io.cockroachdb.pest.web.LinkRelations.OPERATOR_REL;
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

    private Link clusterLink(Cluster cluster, int nodeId) {
        Link baseUrl = cluster.getNodeById(nodeId).getServiceLink();
        return hypermediaClient.from(baseUrl)
                .follow(curied(CURIE_NAMESPACE, CLUSTERS_REL).value())
                .asTemplatedLink();
    }

    private Link operatorLink(Cluster cluster, int nodeId) {
        return hypermediaClient.from(cluster.getNodeById(nodeId).getServiceLink())
                .follow(curied(CURIE_NAMESPACE, CLUSTERS_REL).value())
                .follow(Hop.rel(curied(CURIE_NAMESPACE, LinkRelations.OPERATOR_TEMPLATE_REL).value())
                        .withParameter("clusterType", cluster.getClusterType()))
                .follow(curied(CURIE_NAMESPACE, OPERATOR_REL).value())
                .asTemplatedLink();
    }

    @Override
    public String certs(Cluster cluster, List<Integer> nodeIds, Map<Integer, List<Path>> keyFiles) {
        localClusterOperator.certs(cluster, nodeIds, keyFiles);

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
    public String install(Cluster cluster, Integer nodeId) {
        Link operatorLink = operatorLink(cluster, nodeId);

        Link actionLink = hypermediaClient.from(operatorLink)
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
    public String startNode(Cluster cluster, Integer nodeId) {
        Link operatorLink = operatorLink(cluster, nodeId);

        Link actionLink = hypermediaClient.from(operatorLink)
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
    public String stopNode(Cluster cluster, Integer nodeId) {
        Link operatorLink = operatorLink(cluster, nodeId);

        Link actionLink = hypermediaClient.from(operatorLink)
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
    public String killNode(Cluster cluster, Integer nodeId) {
        Link operatorLink = operatorLink(cluster, nodeId);

        Link actionLink = hypermediaClient.from(operatorLink)
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
    public String init(Cluster cluster, Integer nodeId) {
        Link operatorLink = operatorLink(cluster, nodeId);

        Link actionLink = hypermediaClient.from(operatorLink)
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
    public String wipe(Cluster cluster, Integer nodeId, boolean all) {
        Link operatorLink = operatorLink(cluster, nodeId);

        Link actionLink = hypermediaClient.from(operatorLink)
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
    public String sqlNode(Cluster cluster, Integer nodeId) {
        return localClusterOperator.sqlNode(cluster, nodeId);
    }

    @Override
    public String statusNode(Cluster cluster, Integer nodeId) {
        Link operatorLink = operatorLink(cluster, nodeId);

        Link actionLink = hypermediaClient.from(operatorLink)
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

    @Override
    public String disruptNode(Cluster cluster, Integer nodeId) {
        return killNode(cluster, nodeId);
    }

    @Override
    public String recoverNode(Cluster cluster, Integer nodeId) {
        return startNode(cluster, nodeId);
    }

    @Override
    public String disruptLocality(Cluster cluster, String locality) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String recoverLocality(Cluster cluster, String locality) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String startToxiproxyServer() {
        return localClusterOperator.startToxiproxyServer();
    }

    @Override
    public String stopToxiproxyServer() {
        return localClusterOperator.stopToxiproxyServer();
    }

    @Override
    public String genHAProxyCfg(Cluster cluster, Integer nodeId) {
        Link operatorLink = operatorLink(cluster, nodeId);

        Link actionLink = hypermediaClient.from(operatorLink)
                .follow(curied(CURIE_NAMESPACE, NODE_GEN_HAPROXY_REL).value())
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
    public String startHAProxy(Cluster cluster, Integer nodeId) {
        Link operatorLink = operatorLink(cluster, nodeId);

        Link actionLink = hypermediaClient.from(operatorLink)
                .follow(curied(CURIE_NAMESPACE, NODE_START_HAPROXY_REL).value())
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
    public String stopHAProxy(Cluster cluster, Integer nodeId) {
        Link operatorLink = operatorLink(cluster, nodeId);

        Link actionLink = hypermediaClient.from(operatorLink)
                .follow(curied(CURIE_NAMESPACE, NODE_STOP_HAPROXY_REL).value())
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
