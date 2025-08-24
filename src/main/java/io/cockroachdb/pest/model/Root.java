package io.cockroachdb.pest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
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
