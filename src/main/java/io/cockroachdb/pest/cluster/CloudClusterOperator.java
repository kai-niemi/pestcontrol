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

import io.cockroachdb.pest.model.ClusterSettings;
import io.cockroachdb.pest.model.ClusterType;
import io.cockroachdb.pest.model.Locality;
import io.cockroachdb.pest.model.NodeSettings;
import io.cockroachdb.pest.model.schema.DisruptorSpecifications;
import io.cockroachdb.pest.model.schema.RegionalDisruptorSpecification;

@Component
public class CloudClusterOperator implements ClusterOperator {
    private static final String CLOUD_API_BASE = "https://cockroachlabs.cloud/api/v1";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public boolean supports(ClusterType clusterType) {
        return EnumSet.of(ClusterType.cloud_dedicated).contains(clusterType);
    }

    @Override
    public String certs(ClusterSettings clusterSettings, List<Integer> nodeIds, Map<Integer, List<Path>> keyFiles) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String install(ClusterSettings clusterSettings, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String init(ClusterSettings clusterSettings, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String wipe(ClusterSettings cluster, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String startProxyClient(ClusterSettings cluster, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String killNode(ClusterSettings clusterSettings, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String startNode(ClusterSettings clusterSettings, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String stopNode(ClusterSettings clusterSettings, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String sqlNode(ClusterSettings cluster, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String disruptNode(ClusterSettings clusterSettings, Integer nodeId) {
        NodeSettings nodeSettings = clusterSettings.findNodePropertiesById(nodeId);

        final Locality locality = Locality.fromTiers(nodeSettings.getLocality());

        final String region = locality.getTiers().stream()
                .filter(tier -> tier.getKey().equals("region"))
                .findFirst().orElseThrow(() ->
                        new IllegalArgumentException(
                                "No locality or region key found: " + nodeSettings.getLocality()))
                .getValue();

        final RegionalDisruptorSpecification regionalDisruptorSpecification = new RegionalDisruptorSpecification();
        {
            regionalDisruptorSpecification.setIsWholeRegion(false);
            regionalDisruptorSpecification.setRegionCode(region);
            regionalDisruptorSpecification.getPods().add("cockroachdb-" + nodeSettings.getId());
        }

        final DisruptorSpecifications disruptorSpecifications = new DisruptorSpecifications();
        disruptorSpecifications.addRegionalDisruptorSpecification(regionalDisruptorSpecification);

        final String bearerToken = clusterSettings.getApiKey();

        try {
            ResponseEntity<String> responseEntity = RestClient.create()
                    .put()
                    .uri(CLOUD_API_BASE + "/clusters/%s/disrupt"
                            .formatted(clusterSettings.getClusterId()))
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
    public String recoverNode(ClusterSettings clusterSettings, Integer nodeId) {
        String bearerToken = clusterSettings.getApiKey();

        ResponseEntity<String> responseEntity = RestClient.create()
                .put()
                .uri(CLOUD_API_BASE + "/clusters/%s/disrupt"
                        .formatted(clusterSettings.getClusterId()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                .retrieve()
                .toEntity(String.class);

        logger.info("Disrupt recovery command successful: %s".formatted(responseEntity.getBody()));
        return responseEntity.getBody();
    }

    @Override
    public String disruptLocality(ClusterSettings clusterSettings, String tiers) {
        Locality locality = Locality.fromTiers(tiers);

        final RegionalDisruptorSpecification regionalDisruptorSpecification = new RegionalDisruptorSpecification();
        {
            regionalDisruptorSpecification.setIsWholeRegion(true);
            regionalDisruptorSpecification.setRegionCode(locality
                    .findRegionTier().orElseThrow(
                            () -> new UnsupportedOperationException("No region tier found in locality: " + locality)));
        }

        final DisruptorSpecifications disruptorSpecifications = new DisruptorSpecifications();
        disruptorSpecifications.addRegionalDisruptorSpecification(regionalDisruptorSpecification);

        final String bearerToken = clusterSettings.getApiKey();

        try {
            ResponseEntity<String> responseEntity = RestClient.create()
                    .put()
                    .uri(CLOUD_API_BASE + "/clusters/%s/disrupt"
                            .formatted(clusterSettings.getClusterId()))
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
    public String recoverLocality(ClusterSettings clusterSettings, String locality) {
        String bearerToken = clusterSettings.getApiKey();

        ResponseEntity<String> responseEntity = RestClient.create()
                .put()
                .uri(CLOUD_API_BASE + "/clusters/%s/disrupt"
                        .formatted(clusterSettings.getClusterId()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                .retrieve()
                .toEntity(String.class);

        logger.info("Disrupt recovery command successful: %s".formatted(responseEntity.getBody()));
        return responseEntity.getBody();
    }

    @Override
    public String startProxyServer(ClusterSettings cluster) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String stopProxyServer(ClusterSettings cluster) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String startLoadBalancer(ClusterSettings cluster, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String stopLoadBalancer(ClusterSettings cluster) {
        throw new UnsupportedOperationException();
    }
}
