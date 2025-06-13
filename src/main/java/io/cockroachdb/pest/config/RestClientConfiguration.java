package io.cockroachdb.pest.config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.EnumSet;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.client.RestClientSsl;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.ssl.NoSuchSslBundleException;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import io.cockroachdb.pest.cluster.ClientErrorException;
import io.cockroachdb.pest.cluster.ServerErrorException;
import io.cockroachdb.pest.model.ApplicationProperties;
import io.cockroachdb.pest.model.ClusterType;
import io.cockroachdb.pest.shell.client.HypermediaClient;

@Configuration
public class RestClientConfiguration implements RestTemplateCustomizer {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApplicationProperties applicationProperties;

    @Override
    public void customize(RestTemplate restTemplate) {
        int maxTotal = applicationProperties.getHttp().getMaxTotal();
        int maxConnPerRoute = applicationProperties.getHttp().getMaxConnPerRoute();

        if (maxConnPerRoute <= 0 || maxTotal <= 0) {
            maxConnPerRoute = Runtime.getRuntime().availableProcessors() * 8;
            maxTotal = maxConnPerRoute * 2;
        }

        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultSocketConfig(SocketConfig.custom()
//                        .setSoTimeout(Timeout.ofSeconds(10))
                        .build())
                .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.STRICT)
                .setConnPoolPolicy(PoolReusePolicy.LIFO)
                .setMaxConnTotal(maxTotal)
                .setMaxConnPerRoute(maxConnPerRoute)
                .build();

        CloseableHttpClient client = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();

        HttpComponentsClientHttpRequestFactory factory
                = new HttpComponentsClientHttpRequestFactory(client);
//        factory.setConnectTimeout(Duration.ofSeconds(5));
//        factory.setReadTimeout(Duration.ofSeconds(5));

        restTemplate.setRequestFactory(factory);
    }

    @Bean
    public HypermediaClient hypermediaClient(RestTemplateBuilder builder) {
        return new HypermediaClient(builder.build());
    }

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
