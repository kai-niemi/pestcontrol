package io.cockroachdb.pest.workload.repository;

import javax.sql.DataSource;

public class UpdateOne extends CyclicWorker {
    public UpdateOne(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void run() {
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            findNext(false)
                    .ifPresent(sampleRepository::updateSingleton);
        });
    }
}
