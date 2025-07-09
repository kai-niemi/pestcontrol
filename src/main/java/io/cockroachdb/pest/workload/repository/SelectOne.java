package io.cockroachdb.pest.workload.repository;

import javax.sql.DataSource;

public class SelectOne extends AbstractSampleWorkload {
    public SelectOne(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void run() {
        jdbcTemplate.execute("select 1");
    }
}
