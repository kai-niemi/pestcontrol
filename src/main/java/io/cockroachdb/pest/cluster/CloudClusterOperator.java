package io.cockroachdb.pest.cluster;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ServerErrorException;

import io.cockroachdb.pest.cluster.model.DisruptorSpecifications;
import io.cockroachdb.pest.cluster.model.RegionalDisruptorSpecification;
import io.cockroachdb.pest.domain.Cluster;
import io.cockroachdb.pest.domain.ClusterType;
import io.cockroachdb.pest.domain.Locality;

@Component
public class CloudClusterOperator implements ClusterOperator {
    private static final String CLOUD_API_BASE = "https://cockroachlabs.cloud/api/v1";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public boolean supports(ClusterType clusterType) {
        return EnumSet.of(ClusterType.cloud_dedicated).contains(clusterType);
    }

    @Override
    public String certs(Cluster cluster, List<Integer> nodeIds, Map<Integer, List<Path>> keyFiles) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String install(Cluster cluster, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String init(Cluster cluster, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String wipe(Cluster cluster, Integer nodeId, boolean all) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String killNode(Cluster cluster, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String startNode(Cluster cluster, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String stopNode(Cluster cluster, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String statusNode(Cluster cluster, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String sqlNode(Cluster cluster, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String disruptNode(Cluster cluster, Integer nodeId) {
        Cluster.Node node = cluster.getNodeById(nodeId);

        final Locality locality = Locality.fromTiers(node.getLocality());

        final String region = locality.getTiers().stream()
                .filter(tier -> tier.getKey().equals("region"))
                .findFirst().orElseThrow(() ->
                        new IllegalArgumentException(
                                "No locality or region key found: " + node.getLocality()))
                .getValue();

        final RegionalDisruptorSpecification regionalDisruptorSpecification = new RegionalDisruptorSpecification();
        {
            regionalDisruptorSpecification.setIsWholeRegion(false);
            regionalDisruptorSpecification.setRegionCode(region);
            regionalDisruptorSpecification.getPods().add("cockroachdb-" + node.getId());
        }

        final DisruptorSpecifications disruptorSpecifications = new DisruptorSpecifications();
        disruptorSpecifications.addRegionalDisruptorSpecification(regionalDisruptorSpecification);

        final String bearerToken = cluster.getApiKey();

        try {
            ResponseEntity<String> responseEntity = RestClient.create()
                    .put()
                    .uri(CLOUD_API_BASE + "/clusters/%s/disrupt"
                            .formatted(cluster.getClusterId()))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                    .body(disruptorSpecifications)
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(String.class);
            logger.info("Disrupt command successful: %s".formatted(responseEntity.getBody()));
            return responseEntity.getBody();
        } catch (HttpClientErrorException e) {
            throw new ClientErrorException("Disrupt API command failed due to client error: "
                                           + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
            throw new ServerErrorException("Disrupt API command failed due to server error: "
                                           + e.getMessage(), e);
        }
    }

    @Override
    public String recoverNode(Cluster cluster, Integer nodeId) {
        String bearerToken = cluster.getApiKey();

        ResponseEntity<String> responseEntity = RestClient.create()
                .put()
                .uri(CLOUD_API_BASE + "/clusters/%s/disrupt"
                        .formatted(cluster.getClusterId()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                .retrieve()
                .toEntity(String.class);

        logger.info("Disrupt recovery command successful: %s".formatted(responseEntity.getBody()));
        return responseEntity.getBody();
    }

    @Override
    public String disruptLocality(Cluster cluster, String tiers) {
        Locality locality = Locality.fromTiers(tiers);

        final RegionalDisruptorSpecification regionalDisruptorSpecification = new RegionalDisruptorSpecification();
        {
            regionalDisruptorSpecification.setIsWholeRegion(true);
            regionalDisruptorSpecification.setRegionCode(locality
                    .findRegionTier().orElseThrow(
                            () -> new UnsupportedOperationException("No region tier found in locality: " + locality))
                    .getKey());
        }

        final DisruptorSpecifications disruptorSpecifications = new DisruptorSpecifications();
        disruptorSpecifications.addRegionalDisruptorSpecification(regionalDisruptorSpecification);

        final String bearerToken = cluster.getApiKey();

        try {
            ResponseEntity<String> responseEntity = RestClient.create()
                    .put()
                    .uri(CLOUD_API_BASE + "/clusters/%s/disrupt"
                            .formatted(cluster.getClusterId()))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                    .body(disruptorSpecifications)
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(String.class);
            logger.info("Disrupt command successful: %s".formatted(responseEntity.getBody()));
            return responseEntity.getBody();
        } catch (HttpClientErrorException e) {
            throw new ClientErrorException("Disrupt API command failed due to client error: "
                                           + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
            throw new ServerErrorException("Disrupt API command failed due to server error: "
                                           + e.getMessage(), e);
        }
    }

    @Override
    public String recoverLocality(Cluster cluster, String locality) {
        String bearerToken = cluster.getApiKey();

        ResponseEntity<String> responseEntity = RestClient.create()
                .put()
                .uri(CLOUD_API_BASE + "/clusters/%s/disrupt"
                        .formatted(cluster.getClusterId()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                .retrieve()
                .toEntity(String.class);

        logger.info("Disrupt recovery command successful: %s".formatted(responseEntity.getBody()));
        return responseEntity.getBody();
    }

    @Override
    public String startToxiproxyServer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String stopToxiproxyServer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String genHAProxyCfg(Cluster cluster, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String startHAProxy(Cluster cluster, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String stopHAProxy(Cluster cluster, Integer nodeId) {
        throw new UnsupportedOperationException();
    }
}
