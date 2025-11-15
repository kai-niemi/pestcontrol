package io.cockroachdb.pest.web.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.hateoas.Link;

import io.cockroachdb.pest.cluster.model.NodeModel;
import io.cockroachdb.pest.cluster.model.NodeStatus;
import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.model.Locality;

public class ClusterModel {
    private final Cluster cluster;

    private boolean available;

    private Collection<NodeModel> nodeModels = List.of();

    public ClusterModel(Cluster cluster) {
        this.cluster = cluster;
    }

    public String getClusterId() {
        return cluster.getClusterId();
    }

    public void setNodeModels(Collection<NodeModel> nodeModels) {
        this.nodeModels = nodeModels;
    }

    public boolean isDifferent(Collection<NodeModel> otherModel) {
        return this.nodeModels.size() != otherModel.size();
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public Link getAdminLink() {
        return Link.of(cluster.getAdminUrl());
    }

    /**
     * Find locality tiers up to a given sublevel.
     *
     * @param level the sublevel (1-based)
     * @return list of tiers
     */
    public List<Locality> getLocalities(int level) {
        // LHS for ordering and de-dup
        Set<Locality> subLocalities = new LinkedHashSet<>();

        nodeModels.forEach(node -> {
            Locality locality = node.getNodeDetail().getLocality();

            Locality subLocality = new Locality(new ArrayList<>(locality.getTiers()
                    .stream()
                    .limit(level)
                    .toList()));

            subLocalities.stream().filter(x -> x.getTiers().equals(subLocality.getTiers()))
                    .findFirst()
                    .ifPresentOrElse(x -> {
                    }, () -> subLocalities.add(subLocality));
        });

        return new ArrayList<>(subLocalities);
    }

    /**
     * Find nodes matching a given list of locality tiers.
     *
     * @param tiers the locality tiers
     * @return list of matching nodes, sorted by node ID in ascending order
     */
    public List<NodeModel> getNodes(List<Locality.Tier> tiers) {
        return nodeModels.stream()
                .filter(node -> node.getNodeDetail().getLocality().matches(tiers))
                .sorted(Comparator.comparing(NodeModel::getNodeId))
                .toList();
    }

    public String getCardClass(NodeStatus nodeStatus) {
        if (!isAvailable()) {
            return "border-warning alert-warning";
        }

        boolean isLive = "true".equals(nodeStatus.getIsLive());  // up and running
        boolean isAvailable = "true".equals(nodeStatus.getIsAvailable()); // part of quorum
        if (isLive) {
            if (isAvailable) {
                return "border-success alert-success";
            }
            return "border-warning alert-warning";
        } else {
            if (isAvailable) {
                return "border-warning alert-warning";
            }
            return "border-danger alert-danger";
        }
    }

    public String getCardImage(NodeStatus nodeStatus) {
        if (!isAvailable()) {
            return "#db-warn";
        }

        boolean isLive = "true".equals(nodeStatus.getIsLive());  // up and running
        boolean isAvailable = "true".equals(nodeStatus.getIsAvailable()); // part of quorum
        if (isLive) {
            if (isAvailable) {
                return "#db-ok";
            }
            return "#db-warn";
        } else {
            if (isAvailable) {
                return "#db-warn";
            }
            return "#db-fail";
        }
    }
}
