package io.cockroachdb.pest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Root {
    @JsonProperty("application")
    private ApplicationSettings applicationSettings;

    public Root(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public ApplicationSettings getApplicationProperties() {
        return applicationSettings;
    }

    public void setApplicationProperties(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }
}
