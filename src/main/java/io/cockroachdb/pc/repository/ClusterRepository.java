package io.cockroachdb.pc.repository;

public interface ClusterRepository {
    String queryNodeStatus();

    String queryNodeStatusById(Integer id);
}
