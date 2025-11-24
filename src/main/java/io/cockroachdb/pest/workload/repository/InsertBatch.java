package io.cockroachdb.pest.workload.repository;

import javax.sql.DataSource;

public class InsertBatch extends AbstractSampleWorkload {
    public InsertBatch(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void run() {
        sampleRepository.insertBatch(32);
    }
}
