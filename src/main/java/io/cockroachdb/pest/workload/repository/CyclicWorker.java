package io.cockroachdb.pest.workload.repository;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import javax.sql.DataSource;

public abstract class CyclicWorker extends AbstractSampleWorkload {
    private final AtomicReference<Optional<SampleEntity>> latestEntity
            = new AtomicReference<>(Optional.empty());

    public CyclicWorker(DataSource dataSource) {
        super(dataSource);
    }

    protected Optional<SampleEntity> findNext(boolean followerRead) {
        Optional<SampleEntity> e = latestEntity.get();
        if (e.isPresent()) {
            e = sampleRepository.findByNextId(e.get().getId(), followerRead);
        }
        if (e.isEmpty()) {
            e = sampleRepository.findFirst(followerRead);
        }
        latestEntity.set(e);
        return e;
    }
}

