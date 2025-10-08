package io.cockroachdb.pest.util;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import org.springframework.core.io.FileSystemResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.hateoas.mediatype.hal.HalLinkDiscoverer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class HypermediaClient {
    private static final List<MediaType> ACCEPT_TYPES = List.of(MediaTypes.HAL_JSON);

    private final RestTemplate restTemplate;

    public HypermediaClient(RestTemplate restTemplate) {
        Objects.requireNonNull(restTemplate);
        this.restTemplate = restTemplate;
    }

    public Traverson from(Link link) {
        Objects.requireNonNull(link);
        return from(link.getHref());
    }

    public Traverson from(String href) {
        Objects.requireNonNull(href);
        return from(URI.create(href), ACCEPT_TYPES);
    }

    public Traverson from(Link link, List<MediaType> mediaTypes) {
        Objects.requireNonNull(link);
        return from(URI.create(link.getHref()), mediaTypes);
    }

    public Traverson from(URI uri, List<MediaType> mediaTypes) {
        Objects.requireNonNull(uri);
        Traverson traverson = new Traverson(uri, mediaTypes);
        traverson.setRestOperations(restTemplate);
        traverson.setLinkDiscoverers(List.of(new HalLinkDiscoverer()));
        return traverson;
    }

    public ResponseEntity<String> get(Link link) {
        Objects.requireNonNull(link);
        return restTemplate.getForEntity(link.toUri(), String.class);
    }

    public ResponseEntity<String> upload(Link link, List<Path> files) {
        Objects.requireNonNull(link);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        files.forEach(path -> body.add("files", new FileSystemResource(path)));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, httpHeaders);

        return restTemplate.exchange(link.getHref(), HttpMethod.POST, requestEntity, String.class);
    }

    public ResponseEntity<String> post(Link link) {
        Objects.requireNonNull(link);
        return restTemplate.postForEntity(link.getTemplate().expand(), null, String.class);
    }

    public <T> ResponseEntity<T> post(Link link, Class<T> responseType) {
        Objects.requireNonNull(link);
        return restTemplate.postForEntity(
                link.getTemplate().expand(),
                null,
                responseType);
    }

    public <T> ResponseEntity<T> post(Link link, Object request, Class<T> responseType) {
        Objects.requireNonNull(link);
        return restTemplate.postForEntity(
                link.getTemplate().expand(),
                request,
                responseType);
    }

    public <T> ResponseEntity<T> put(Link link, Class<T> responseType) {
        Objects.requireNonNull(link);
        return restTemplate.exchange(
                link.getTemplate().expand(),
                HttpMethod.PUT,
                null,
                responseType);
    }

    public <T> ResponseEntity<T> delete(Link link, Class<T> responseType) {
        Objects.requireNonNull(link);
        return restTemplate.exchange(
                link.getTemplate().expand(),
                HttpMethod.DELETE,
                null,
                responseType);
    }
}
