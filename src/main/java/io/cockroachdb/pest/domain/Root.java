package io.cockroachdb.pest.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Root {
    @JsonProperty("application")
    private ApplicationProperties applicationProperties;

    public Root(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public ApplicationProperties getApplicationProperties() {
        return applicationProperties;
    }

    public void setApplicationProperties(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }
}
