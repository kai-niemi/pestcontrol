package io.cockroachdb.pest.workload;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import javax.sql.DataSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.jdbc.core.JdbcTemplate;

import io.cockroachdb.pest.AbstractIntegrationTest;
import io.cockroachdb.pest.config.ClosableDataSource;
import io.cockroachdb.pest.workload.repository.JdbcSampleRepository;
import io.cockroachdb.pest.workload.repository.SampleEntity;
import io.cockroachdb.pest.workload.repository.SampleRepository;
import io.cockroachdb.pest.workload.repository.WorkloadType;

public class WorkloadTest extends AbstractIntegrationTest {
    private SampleRepository sampleRepository;

    private DataSource dataSource;

    @Autowired
    private Function<DataSourceProperties, ClosableDataSource> dataSourceFactory;

    @BeforeAll
    public void setupTestOnce() {
        this.dataSource = dataSourceFactory.apply(
                applicationProperties.getDataSourceProperties("integration-test"));

        logger.info("Connected to: %s".formatted(
                new JdbcTemplate(dataSource)
                        .queryForObject("select version()", String.class)));

        this.sampleRepository = new JdbcSampleRepository(dataSource);
        this.sampleRepository.deleteAll();
    }

    @Order(0)
    @Test
    public void whenStartingInsertWorkload_thenExpectRows() {
        List<SampleEntity> before = sampleRepository.findAll(65536);

        Runnable action = WorkloadType.singleton_insert
                .createTask(dataSource);
        IntStream.rangeClosed(1, 10).forEach(value -> {
            try {
                action.run();
            } catch (Exception e) {
                Assertions.fail(e);
            }
        });

        List<SampleEntity> after = sampleRepository.findAll(65536);
        Assertions.assertEquals(before.size() + 10, after.size());
    }

    @Order(1)
    @Test
    public void whenStartingBatchInsertWorkload_thenExpectRows() {
        List<SampleEntity> before = sampleRepository.findAll(65536);

        Runnable action = WorkloadType.batch_insert
                .createTask(dataSource);
        IntStream.rangeClosed(1, 10).forEach(value -> {
            try {
                action.run();
            } catch (Exception e) {
                Assertions.fail(e);
            }
        });

        List<SampleEntity> after = sampleRepository.findAll(65536);
        Assertions.assertEquals(before.size() + 10 * 32, after.size());
    }

    @Order(2)
    @Test
    public void whenStartingUpdateWorkload_thenExpectRowsAffected() {
        Runnable action = WorkloadType.point_read_update
                .createTask(dataSource);
        IntStream.rangeClosed(1, 10).forEach(value -> {
            try {
                action.run();
            } catch (Exception e) {
                Assertions.fail(e);
            }
        });
    }

    @Order(4)
    @Test
    public void whenStartingDeleteWorkload_thenExpectRowsAffected() {
        Runnable action = WorkloadType.point_read_delete
                .createTask(dataSource);
        IntStream.rangeClosed(1, 10).forEach(value -> {
            try {
                action.run();
            } catch (Exception e) {
                Assertions.fail(e);
            }
        });
    }

    @Order(5)
    @Test
    public void whenStartingReadWorkload_thenExpectRows() {
        Runnable action = WorkloadType.point_read
                .createTask(dataSource);
        IntStream.rangeClosed(1, 10).forEach(value -> {
            try {
                action.run();
            } catch (Exception e) {
                Assertions.fail(e);
            }
        });

        Runnable action2 = WorkloadType.point_read_historical
                .createTask(dataSource);
        IntStream.rangeClosed(1, 10).forEach(value -> {
            try {
                action2.run();
            } catch (Exception e) {
                Assertions.fail(e);
            }
        });
    }

    @Order(5)
    @Test
    public void whenStartingScanWorkload_thenExpectRows() {
        Runnable action = WorkloadType.full_scan
                .createTask(dataSource);
        IntStream.rangeClosed(1, 10).forEach(value -> {
            try {
                action.run();
            } catch (Exception e) {
                Assertions.fail(e);
            }
        });
    }
}
