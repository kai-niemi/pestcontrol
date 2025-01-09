package io.cockroachdb.pc.workload;

import java.util.List;
import java.util.stream.IntStream;

import javax.sql.DataSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import io.cockroachdb.pc.AbstractIntegrationTest;
import io.cockroachdb.pc.repository.JdbcProfileRepository;
import io.cockroachdb.pc.repository.ProfileEntity;
import io.cockroachdb.pc.repository.ProfileRepository;
import io.cockroachdb.pc.workload.profile.WorkloadType;

public class ProfileWorkloadsTest extends AbstractIntegrationTest {
    private ProfileRepository profileRepository;

    private DataSource dataSource;

    @BeforeAll
    public void setupTestOnce() {
        this.dataSource = applicationProperties.getDataSource("integration-test");

        logger.info("Connected to: %s".formatted(
                new JdbcTemplate(dataSource)
                        .queryForObject("select version()", String.class)));

        this.profileRepository = new JdbcProfileRepository(dataSource);
        this.profileRepository.deleteAll();
    }

    @Order(0)
    @Test
    public void whenStartingInsertWorkload_thenExpectRows() {
        List<ProfileEntity> before = profileRepository.findAll(65536);

        Runnable action = WorkloadType.singleton_insert
                .createTask(dataSource);
        IntStream.rangeClosed(1, 10).forEach(value -> {
            try {
                action.run();
            } catch (Exception e) {
                Assertions.fail(e);
            }
        });

        List<ProfileEntity> after = profileRepository.findAll(65536);
        Assertions.assertEquals(before.size() + 10, after.size());
    }

    @Order(1)
    @Test
    public void whenStartingBatchInsertWorkload_thenExpectRows() {
        List<ProfileEntity> before = profileRepository.findAll(65536);

        Runnable action = WorkloadType.batch_insert
                .createTask(dataSource);
        IntStream.rangeClosed(1, 10).forEach(value -> {
            try {
                action.run();
            } catch (Exception e) {
                Assertions.fail(e);
            }
        });

        List<ProfileEntity> after = profileRepository.findAll(65536);
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
