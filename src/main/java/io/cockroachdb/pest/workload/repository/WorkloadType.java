package io.cockroachdb.pest.workload.repository;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

public enum WorkloadType {
    singleton_insert("Singleton insert",
            "Single insert statement") {
        @Override
        public Runnable createTask(DataSource dataSource) {
            return new InsertOne(dataSource);
        }
    },
    batch_insert("Batch insert",
            "Batch insert statement of 32 items") {
        @Override
        public Runnable createTask(DataSource dataSource) {
            return new InsertBatch(dataSource);
        }
    },
    point_read_update("Point read and update",
            "Point lookup read followed by an update") {
        @Override
        public Runnable createTask(DataSource dataSource) {
            return new UpdateOne(dataSource);
        }
    },
    point_read_delete("Point read and delete",
            "Point lookup read followed by a delete") {
        @Override
        public Runnable createTask(DataSource dataSource) {
            return new DeleteOne(dataSource);
        }
    },
    point_read("Point read authoritative",
            "Single authoritative point lookup read") {
        @Override
        public Runnable createTask(DataSource dataSource) {
            return new PointRead(dataSource, false);
        }
    },
    point_read_historical("Point read historical",
            "Single point lookup exact staleness read") {
        @Override
        public Runnable createTask(DataSource dataSource) {
            return new PointRead(dataSource, true);
        }
    },
    full_scan("Full table scan",
            "A full table scan (limited)") {
        @Override
        public Runnable createTask(DataSource dataSource) {
            return new FullScan(dataSource);
        }
    },
    select_one("Select one",
            "A basic 'select 1' statement") {
        @Override
        public Runnable createTask(DataSource dataSource) {
            return new SelectOne(dataSource);
        }
    },
    random_wait("Random wait",
            "A random delay between 0-5ms with .5 to 2s outliers") {
        @Override
        public Runnable createTask(DataSource dataSource) {
            return () -> {
                ThreadLocalRandom random = ThreadLocalRandom.current();
                try {
                    if (random.nextDouble(1.0) > 0.95) {
                        TimeUnit.MILLISECONDS.sleep(random.nextLong(500, 2000));
                    } else {
                        TimeUnit.MILLISECONDS.sleep(random.nextLong(0, 5));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            };
        }
    },
    fixed_wait("Fixed wait",
            "A fixed wait of 500ms") {
        @Override
        public Runnable createTask(DataSource dataSource) {
            return () -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            };
        }
    };

    private final String displayValue;

    private final String description;

    WorkloadType(String displayValue, String description) {
        this.displayValue = displayValue;
        this.description = description;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public String getDescription() {
        return description;
    }

    public abstract Runnable createTask(DataSource dataSource);
}
