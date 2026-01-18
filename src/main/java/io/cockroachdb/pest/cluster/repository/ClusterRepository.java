package io.cockroachdb.pest.cluster.repository;

public interface ClusterRepository {
    String queryNodeStatus();

    String queryNodeStatusById(Integer id);
}
