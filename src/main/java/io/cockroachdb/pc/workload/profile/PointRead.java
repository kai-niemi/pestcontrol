package io.cockroachdb.pc.workload.profile;

import javax.sql.DataSource;

public class PointRead extends CyclicWorker {
    private final boolean followerRead;

    public PointRead(DataSource dataSource, boolean followerRead) {
        super(dataSource);
        this.followerRead = followerRead;
    }

    @Override
    public void run() {
        findNextProfile(followerRead);
    }
}
