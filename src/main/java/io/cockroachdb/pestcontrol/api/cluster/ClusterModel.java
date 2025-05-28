package io.cockroachdb.pestcontrol.api.cluster;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.cockroachdb.pestcontrol.api.LinkRelations;
import io.cockroachdb.pestcontrol.model.ClusterProperties;

@Relation(value = LinkRelations.CLUSTER_REL,
        collectionRelation = LinkRelations.CLUSTERS_REL)
@JsonPropertyOrder({"links", "embedded", "templates"})
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
public class ClusterModel extends RepresentationModel<ClusterModel> {
    public static ClusterModel from(ClusterProperties clusterProperties) {
        return new ClusterModel(clusterProperties);
    }

    private final ClusterProperties clusterProperties;

    public ClusterModel(ClusterProperties clusterProperties) {
        this.clusterProperties = clusterProperties;
    }

    public ClusterProperties getClusterProperties() {
        return clusterProperties;
    }
}
