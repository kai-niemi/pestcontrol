package io.cockroachdb.pest.web.model;

import org.springframework.hateoas.EntityModel;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.cockroachdb.pest.model.Cluster;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClusterModel extends EntityModel<Cluster> {
    public ClusterModel(Cluster cluster) {
        super(cluster);
    }

    public String getClusterId() {
        return getContent().getClusterId();
    }
}
