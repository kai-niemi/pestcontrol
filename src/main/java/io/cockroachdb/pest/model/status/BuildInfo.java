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
        "goVersion",
        "tag",
        "time",
        "revision",
        "cgoCompiler",
        "cgoTargetTriple",
        "platform",
        "distribution",
        "type",
        "channel",
        "envChannel",
        "enabledAssertions",
        "dependencies"
})
public class BuildInfo {
    @JsonProperty("goVersion")
    private String goVersion;
    @JsonProperty("tag")
    private String tag;
    @JsonProperty("time")
    private String time;
    @JsonProperty("revision")
    private String revision;
    @JsonProperty("cgoCompiler")
    private String cgoCompiler;
    @JsonProperty("cgoTargetTriple")
    private String cgoTargetTriple;
    @JsonProperty("platform")
    private String platform;
    @JsonProperty("distribution")
    private String distribution;
    @JsonProperty("type")
    private String type;
    @JsonProperty("channel")
    private String channel;
    @JsonProperty("envChannel")
    private String envChannel;
    @JsonProperty("enabledAssertions")
    private Boolean enabledAssertions;
    @JsonProperty("dependencies")
    private Object dependencies;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new LinkedHashMap<>();

    @JsonProperty("goVersion")
    public String getGoVersion() {
        return goVersion;
    }

    @JsonProperty("goVersion")
    public void setGoVersion(String goVersion) {
        this.goVersion = goVersion;
    }

    @JsonProperty("tag")
    public String getTag() {
        return tag;
    }

    @JsonProperty("tag")
    public void setTag(String tag) {
        this.tag = tag;
    }

    @JsonProperty("time")
    public String getTime() {
        return time;
    }

    @JsonProperty("time")
    public void setTime(String time) {
        this.time = time;
    }

    @JsonProperty("revision")
    public String getRevision() {
        return revision;
    }

    @JsonProperty("revision")
    public void setRevision(String revision) {
        this.revision = revision;
    }

    @JsonProperty("cgoCompiler")
    public String getCgoCompiler() {
        return cgoCompiler;
    }

    @JsonProperty("cgoCompiler")
    public void setCgoCompiler(String cgoCompiler) {
        this.cgoCompiler = cgoCompiler;
    }

    @JsonProperty("cgoTargetTriple")
    public String getCgoTargetTriple() {
        return cgoTargetTriple;
    }

    @JsonProperty("cgoTargetTriple")
    public void setCgoTargetTriple(String cgoTargetTriple) {
        this.cgoTargetTriple = cgoTargetTriple;
    }

    @JsonProperty("platform")
    public String getPlatform() {
        return platform;
    }

    @JsonProperty("platform")
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    @JsonProperty("distribution")
    public String getDistribution() {
        return distribution;
    }

    @JsonProperty("distribution")
    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("channel")
    public String getChannel() {
        return channel;
    }

    @JsonProperty("channel")
    public void setChannel(String channel) {
        this.channel = channel;
    }

    @JsonProperty("envChannel")
    public String getEnvChannel() {
        return envChannel;
    }

    @JsonProperty("envChannel")
    public void setEnvChannel(String envChannel) {
        this.envChannel = envChannel;
    }

    @JsonProperty("enabledAssertions")
    public Boolean getEnabledAssertions() {
        return enabledAssertions;
    }

    @JsonProperty("enabledAssertions")
    public void setEnabledAssertions(Boolean enabledAssertions) {
        this.enabledAssertions = enabledAssertions;
    }

    @JsonProperty("dependencies")
    public Object getDependencies() {
        return dependencies;
    }

    @JsonProperty("dependencies")
    public void setDependencies(Object dependencies) {
        this.dependencies = dependencies;
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
