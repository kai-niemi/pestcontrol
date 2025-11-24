package io.cockroachdb.pest.workload.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SampleRepository {
    SampleEntity insertSingleton();

    List<SampleEntity> insertBatch(int batchSize);

    void updateSingleton(SampleEntity entity);

    void deleteById(UUID id);

    List<SampleEntity> findAll(int limit);

    Optional<SampleEntity> findFirst(boolean followerRead);

    Optional<SampleEntity> findByNextId(UUID id, boolean followerRead);

    Optional<SampleEntity> findByRandomId();

    void deleteAll();
}
