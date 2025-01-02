package io.cockroachdb.pc.workload.profile;

import javax.sql.DataSource;

public class InsertOne extends CyclicWorker {
    public InsertOne(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void run() {
        profileRepository.insertProfileSingleton();
    }
}
