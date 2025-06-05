package io.cockroachdb.pestcontrol.api.cluster;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.cockroachdb.pestcontrol.api.LinkRelations;
import io.cockroachdb.pestcontrol.model.ClusterProperties;

@Relation(value = LinkRelations.CLUSTER_REL,
        collectionRelation = LinkRelations.CLUSTER_COLL_REL)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
public class ClusterModel extends RepresentationModel<ClusterModel> {
    public static ClusterModel from(ClusterProperties clusterProperties) {
        return new ClusterModel(clusterProperties);
    }

    private ClusterProperties clusterProperties;

    public ClusterModel() {
    }

    public ClusterModel(ClusterProperties clusterProperties) {
        this.clusterProperties = clusterProperties;
    }

    public ClusterProperties getClusterProperties() {
        return clusterProperties;
    }

    public void setClusterProperties(ClusterProperties clusterProperties) {
        this.clusterProperties = clusterProperties;
    }
}
