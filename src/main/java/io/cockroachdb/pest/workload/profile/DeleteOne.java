package io.cockroachdb.pest.workload.profile;

import javax.sql.DataSource;

public class DeleteOne extends CyclicWorker {
    public DeleteOne(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void run() {
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            findNextProfile(false).ifPresent(profileEntity ->
                    profileRepository.deleteProfileById(profileEntity.getId()));
        });
    }
}
