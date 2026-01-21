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
        "networkField",
        "addressField"
})
public class Address {
    @JsonProperty("networkField")
    private String networkField;

    @JsonProperty("addressField")
    private String addressField;

    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new LinkedHashMap<>();

    @JsonProperty("networkField")
    public String getNetworkField() {
        return networkField;
    }

    @JsonProperty("networkField")
    public void setNetworkField(String networkField) {
        this.networkField = networkField;
    }

    @JsonProperty("addressField")
    public String getAddressField() {
        return addressField;
    }

    @JsonProperty("addressField")
    public void setAddressField(String addressField) {
        this.addressField = addressField;
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
