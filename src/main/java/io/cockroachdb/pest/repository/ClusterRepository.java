package io.cockroachdb.pest.repository;

public interface ClusterRepository {
    String queryNodeStatus();

    String queryNodeStatusById(Integer id);
}
