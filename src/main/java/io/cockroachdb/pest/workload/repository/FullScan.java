package io.cockroachdb.pest.workload.repository;

import javax.sql.DataSource;

public class FullScan extends AbstractSampleWorkload {
    public FullScan(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void run() {
        sampleRepository.findByRandomId();
    }
}
