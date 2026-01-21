package io.cockroachdb.pest.model.status;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.validation.Valid;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "majorVal",
        "minorVal",
        "patch",
        "internal"
})

public class ServerVersion {
    @JsonProperty("majorVal")
    private Integer majorVal;

    @JsonProperty("minorVal")
    private Integer minorVal;

    @JsonProperty("patch")
    private Integer patch;

    @JsonProperty("internal")
    private Integer internal;

    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("majorVal")
    public Integer getMajorVal() {
        return majorVal;
    }

    @JsonProperty("majorVal")
    public void setMajorVal(Integer majorVal) {
        this.majorVal = majorVal;
    }

    @JsonProperty("minorVal")
    public Integer getMinorVal() {
        return minorVal;
    }

    @JsonProperty("minorVal")
    public void setMinorVal(Integer minorVal) {
        this.minorVal = minorVal;
    }

    @JsonProperty("patch")
    public Integer getPatch() {
        return patch;
    }

    @JsonProperty("patch")
    public void setPatch(Integer patch) {
        this.patch = patch;
    }

    @JsonProperty("internal")
    public Integer getInternal() {
        return internal;
    }

    @JsonProperty("internal")
    public void setInternal(Integer internal) {
        this.internal = internal;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
