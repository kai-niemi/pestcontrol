package io.cockroachdb.pestcontrol.operator;

import java.util.EnumSet;

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

import io.cockroachdb.pestcontrol.manager.ClientErrorException;
import io.cockroachdb.pestcontrol.model.ClusterProperties;
import io.cockroachdb.pestcontrol.model.ClusterType;
import io.cockroachdb.pestcontrol.model.Locality;
import io.cockroachdb.pestcontrol.model.NodeProperties;
import io.cockroachdb.pestcontrol.schema.DisruptorSpecifications;
import io.cockroachdb.pestcontrol.schema.RegionalDisruptorSpecification;

@Component
public class CloudClusterOperator implements ClusterOperator {
    private static final String CLOUD_API_BASE = "https://cockroachlabs.cloud/api/v1";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public boolean supports(ClusterType clusterType) {
        return EnumSet.of(ClusterType.cloud_dedicated).contains(clusterType);
    }

    @Override
    public void init(ClusterProperties clusterProperties, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void install(ClusterProperties clusterProperties, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void killNode(ClusterProperties clusterProperties, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void startNode(ClusterProperties clusterProperties, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void stopNode(ClusterProperties clusterProperties, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void disruptNode(ClusterProperties clusterProperties, Integer nodeId) {
        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

        final Locality locality = Locality.fromTiers(nodeProperties.getLocality());

        final String region = locality.getTiers().stream()
                .filter(tier -> tier.getKey().equals("region"))
                .findFirst().orElseThrow(() ->
                        new IllegalArgumentException(
                                "No locality or region key found: " + nodeProperties.getLocality()))
                .getValue();

        final RegionalDisruptorSpecification regionalDisruptorSpecification = new RegionalDisruptorSpecification();
        {
            regionalDisruptorSpecification.setIsWholeRegion(false);
            regionalDisruptorSpecification.setRegionCode(region);
            regionalDisruptorSpecification.getPods().add("cockroachdb-" + nodeProperties.getId());
        }

        final DisruptorSpecifications disruptorSpecifications = new DisruptorSpecifications();
        disruptorSpecifications.addRegionalDisruptorSpecification(regionalDisruptorSpecification);

        final String bearerToken = clusterProperties.getApiKey();

        try {
            ResponseEntity<String> responseEntity = RestClient.create()
                    .put()
                    .uri(CLOUD_API_BASE + "/clusters/%s/disrupt"
                            .formatted(clusterProperties.getClusterId()))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                    .body(disruptorSpecifications)
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(String.class);
            logger.info("Disrupt command successful: %s".formatted(responseEntity.getBody()));
        } catch (HttpClientErrorException e) {
            throw new ClientErrorException("Disrupt API command failed due to client error: "
                                           + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
            throw new ServerErrorException("Disrupt API command failed due to server error: "
                                           + e.getMessage(), e);
        }
    }

    @Override
    public void recoverNode(ClusterProperties clusterProperties, Integer nodeId) {
        String bearerToken = clusterProperties.getApiKey();

        ResponseEntity<String> responseEntity = RestClient.create()
                .put()
                .uri(CLOUD_API_BASE + "/clusters/%s/disrupt"
                        .formatted(clusterProperties.getClusterId()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                .retrieve()
                .toEntity(String.class);

        logger.info("Disrupt recovery command successful: %s".formatted(responseEntity.getBody()));
    }

    @Override
    public void disruptNodes(ClusterProperties clusterProperties, String tiers) {
        Locality locality = Locality.fromTiers(tiers);

        final RegionalDisruptorSpecification regionalDisruptorSpecification = new RegionalDisruptorSpecification();
        {
            regionalDisruptorSpecification.setIsWholeRegion(true);
            regionalDisruptorSpecification.setRegionCode(locality
                    .findRegionTierValue().orElseThrow(
                            () -> new UnsupportedOperationException("No region tier found in locality: " + locality)));
        }

        final DisruptorSpecifications disruptorSpecifications = new DisruptorSpecifications();
        disruptorSpecifications.addRegionalDisruptorSpecification(regionalDisruptorSpecification);

        final String bearerToken = clusterProperties.getApiKey();

        try {
            ResponseEntity<String> responseEntity = RestClient.create()
                    .put()
                    .uri(CLOUD_API_BASE + "/clusters/%s/disrupt"
                            .formatted(clusterProperties.getClusterId()))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                    .body(disruptorSpecifications)
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(String.class);
            logger.info("Disrupt command successful: %s".formatted(responseEntity.getBody()));
        } catch (HttpClientErrorException e) {
            throw new ClientErrorException("Disrupt API command failed due to client error: "
                                           + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
            throw new ServerErrorException("Disrupt API command failed due to server error: "
                                           + e.getMessage(), e);
        }
    }

    @Override
    public void recoverNodes(ClusterProperties clusterProperties, String tiers) {
        Locality locality = Locality.fromTiers(tiers);

        String bearerToken = clusterProperties.getApiKey();

        ResponseEntity<String> responseEntity = RestClient.create()
                .put()
                .uri(CLOUD_API_BASE + "/clusters/%s/disrupt"
                        .formatted(clusterProperties.getClusterId()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                .retrieve()
                .toEntity(String.class);

        logger.info("Disrupt recovery command successful: %s".formatted(responseEntity.getBody()));
    }
}
