package io.cockroachdb.pest.web.security;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AuthenticationRequest {
    @NotNull
    @Size(max = 255)
    private String clusterId;

    public @NotNull @Size(max = 255) String getClusterId() {
        return clusterId;
    }
}
