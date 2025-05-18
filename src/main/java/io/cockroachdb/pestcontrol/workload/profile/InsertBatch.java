package io.cockroachdb.pestcontrol.workload.profile;

import javax.sql.DataSource;

public class InsertBatch extends AbstractProfileWorkload {
    public InsertBatch(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void run() {
        profileRepository.insertProfileBatch(32);
    }
}
