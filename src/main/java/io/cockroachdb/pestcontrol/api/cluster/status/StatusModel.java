package io.cockroachdb.pestcontrol.api.cluster.status;

import java.util.Comparator;
import java.util.List;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.cockroachdb.pestcontrol.api.LinkRelations;
import io.cockroachdb.pestcontrol.model.Tier;

/**
 * Representation model for a CockroachDB cluster composed by the locality
 * tiers region, zone and node.
 */
@Relation(value = LinkRelations.CLUSTER_STATUS_REL,
        collectionRelation = LinkRelations.CLUSTER_STATUS_COLL_REL)
@JsonPropertyOrder({"links", "embedded", "templates"})
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
public class StatusModel extends RepresentationModel<StatusModel> {
    public static StatusModel fromId(String clusterId) {
        return new StatusModel(clusterId);
    }

    private final String clusterId;

    private CollectionModel<NodeModel> nodes = CollectionModel.empty();

    private StatusModel(String clusterId) {
        Assert.notNull(clusterId, "clusterId is null");
        this.clusterId = clusterId;
    }

    public String getId() {
        return clusterId;
    }

    public CollectionModel<NodeModel> getNodes() {
        return nodes;
    }

    public void setNodes(CollectionModel<NodeModel> nodes) {
        this.nodes = nodes;
    }

    /**
     * Find nodes matching a given list of locality tiers.
     *
     * @param tiers the locality tiers
     * @return list of matching nodes, sorted by node ID in ascending order
     */
    public List<NodeModel> getNodes(List<Tier> tiers) {
        return nodes
                .getContent()
                .stream()
                .filter(node -> node.getLocality().matches(tiers))
                .sorted(Comparator.comparing(NodeModel::getId))
                .toList();
//                        .sorted((n1, n2) -> n1.getLocality().toTiers()
//                .compareToIgnoreCase(n2.getLocality().toTiers()))
    }
}
