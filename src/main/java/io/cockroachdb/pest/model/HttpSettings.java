package io.cockroachdb.pest.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HttpSettings {
    private Integer maxTotal;

    private Integer maxConnPerRoute;

    public Integer getMaxConnPerRoute() {
        return maxConnPerRoute;
    }

    public void setMaxConnPerRoute(Integer maxConnPerRoute) {
        this.maxConnPerRoute = maxConnPerRoute;
    }

    public Integer getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(Integer maxTotal) {
        this.maxTotal = maxTotal;
    }
}
