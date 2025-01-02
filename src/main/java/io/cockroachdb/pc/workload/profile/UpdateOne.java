package io.cockroachdb.pc.workload.profile;

import javax.sql.DataSource;

public class UpdateOne extends CyclicWorker {
    public UpdateOne(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void run() {
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            findNextProfile(false)
                    .ifPresent(profileRepository::updateProfile);
        });
    }
}
