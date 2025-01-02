package io.cockroachdb.pc.workload.profile;

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
