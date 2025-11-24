package io.cockroachdb.pest.workload.repository;

import javax.sql.DataSource;

public class PointRead extends CyclicWorker {
    private final boolean followerRead;

    public PointRead(DataSource dataSource, boolean followerRead) {
        super(dataSource);
        this.followerRead = followerRead;
    }

    @Override
    public void run() {
        findNext(followerRead);
    }
}
