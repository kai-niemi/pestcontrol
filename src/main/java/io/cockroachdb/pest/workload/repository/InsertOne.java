package io.cockroachdb.pest.workload.repository;

import javax.sql.DataSource;

public class InsertOne extends CyclicWorker {
    public InsertOne(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void run() {
        sampleRepository.insertSingleton();
    }
}
