package io.cockroachdb.pc.config;

import java.nio.charset.StandardCharsets;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.client.RestClientSsl;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.ssl.NoSuchSslBundleException;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import io.cockroachdb.pc.schema.ClusterType;
import io.cockroachdb.pc.service.ClientErrorException;
import io.cockroachdb.pc.service.ServerErrorException;

@Configuration
public class RestClientConfiguration {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Bean
    public RestClientProvider restClientProvider(SslBundles sslBundles, RestClientSsl ssl) {
        return clusterProperties -> EnumSet.of(ClusterType.local_secure, ClusterType.remote_secure)
                .contains(clusterProperties.getClusterType())
                ? sslRestClient(sslBundles, ssl) : defaultRestClient();
    }

    @Bean
    public RestClient sslRestClient(SslBundles sslBundles, RestClientSsl ssl) {
        final SslBundle sslBundle;
        try {
            sslBundle = sslBundles.getBundle("pestcontrol");
        } catch (NoSuchSslBundleException e) {
            logger.warn("Fallback to default RestClient (insecure mode): " + e);
            return defaultRestClient();
        }

        return RestClient
                .builder()
                .apply(ssl.fromBundle(sslBundle))
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (request, response) -> {
                    String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    throw new ClientErrorException(
                            "Request failed due to client error with status " + response.getStatusCode()
                            + ": " + body);
                })
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, (request, response) -> {
                    String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    throw new ServerErrorException(
                            "Request failed due to CockroachDB server error with status " + response.getStatusCode()
                            + ": " + body);
                })
                .build();
    }

    @Bean
    public RestClient defaultRestClient() {
        ClientHttpRequestFactory requestFactory
                = ClientHttpRequestFactoryBuilder.httpComponents().build();

        return RestClient
                .builder()
                .requestFactory(requestFactory)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (request, response) -> {
                    String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    throw new ClientErrorException(
                            "Request failed due to client error with status " + response.getStatusCode()
                            + ": " + body);
                })
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, (request, response) -> {
                    String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    throw new ServerErrorException(
                            "Request failed due to CockroachDB server error with status " + response.getStatusCode()
                            + ": " + body);
                })
                .build();
    }
}
