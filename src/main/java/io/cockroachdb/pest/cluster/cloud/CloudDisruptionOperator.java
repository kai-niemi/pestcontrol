package io.cockroachdb.pest.cluster.cloud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import io.cockroachdb.pest.cluster.ClientErrorException;
import io.cockroachdb.pest.cluster.DisruptionOperator;
import io.cockroachdb.pest.cluster.ServerErrorException;
import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.model.Locality;
import io.cockroachdb.pest.model.Node;
import io.cockroachdb.pest.model.status.DisruptorSpecifications;
import io.cockroachdb.pest.model.status.RegionalDisruptorSpecification;

public class CloudDisruptionOperator implements DisruptionOperator {
    private static final String CLOUD_API_BASE = "https://cockroachlabs.cloud/api/v1";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Cluster cluster;

    public CloudDisruptionOperator(Cluster cluster) {
        this.cluster = cluster;
    }

    @Override
    public String disruptNode(Integer nodeId) {
        Node node = cluster.getNodeById(nodeId);

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

        try {
            ResponseEntity<String> responseEntity = RestClient.create()
                    .put()
                    .uri(CLOUD_API_BASE + "/clusters/%s/disrupt"
                            .formatted(cluster.getClusterId()))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + cluster.getApiKey())
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
    public String recoverNode(Integer nodeId) {
        ResponseEntity<String> responseEntity = RestClient.create()
                .put()
                .uri(CLOUD_API_BASE + "/clusters/%s/disrupt"
                        .formatted(cluster.getClusterId()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + cluster.getApiKey())
                .retrieve()
                .toEntity(String.class);

        logger.info("Disrupt recovery command successful: %s".formatted(responseEntity.getBody()));
        return responseEntity.getBody();
    }

    @Override
    public String disruptLocality(String tiers) {
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

        try {
            ResponseEntity<String> responseEntity = RestClient.create()
                    .put()
                    .uri(CLOUD_API_BASE + "/clusters/%s/disrupt"
                            .formatted(cluster.getClusterId()))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + cluster.getApiKey())
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
    public String recoverLocality(String locality) {
        ResponseEntity<String> responseEntity = RestClient.create()
                .put()
                .uri(CLOUD_API_BASE + "/clusters/%s/disrupt"
                        .formatted(cluster.getClusterId()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + cluster.getApiKey())
                .retrieve()
                .toEntity(String.class);

        logger.info("Disrupt recovery command successful: %s".formatted(responseEntity.getBody()));
        return responseEntity.getBody();
    }
}
