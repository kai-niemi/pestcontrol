package io.cockroachdb.pc.workload.profile;

import javax.sql.DataSource;

public class SelectOne extends AbstractProfileWorkload {
    public SelectOne(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void run() {
        jdbcTemplate.execute("select 1");
    }
}
