package io.cockroachdb.pest.workload.profile;

import javax.sql.DataSource;

public class FullScan extends AbstractProfileWorkload {
    public FullScan(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void run() {
        profileRepository.findByRandomId();
    }
}
