package io.cockroachdb.pest.web.api.toxiproxy;

import org.springframework.hateoas.RepresentationModel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"links", "embedded"})
public class ProxyForm extends RepresentationModel<ProxyForm> {
    @NotNull(message = "required field")
    @Size(min = 1, message = "Name should contain at least 1 character")
    private String name;

    @NotNull(message = "required field")
    @Size(min = 1, message = "Listen address should contain a host name or IP address")
    private String listen;

    @NotNull(message = "required field")
    @Size(min = 1, message = "Upstream address should contain a host name or IP address")
    private String upstream;

    public @NotNull String getListen() {
        return listen;
    }

    public void setListen(@NotNull String listen) {
        this.listen = listen;
    }

    public @NotNull String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    public @NotNull String getUpstream() {
        return upstream;
    }

    public void setUpstream(@NotNull String upstream) {
        this.upstream = upstream;
    }
}
