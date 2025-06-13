package io.cockroachdb.pest.workload;

import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionException;

import io.cockroachdb.pest.workload.model.Metrics;
import io.cockroachdb.pest.workload.model.Problem;
import io.cockroachdb.pest.workload.model.Workload;

@Component
public class WorkloadExecutor {
    /**
     * Transient CockroachDB/PostgresSQL SQL state codes but only 40001 is safe to retry in terms
     * of non-idempotent side effects (like INSERT:s)
     */
    private static final List<String> TRANSIENT_CODES = List.of(
            "40001", "08001", "08003", "08004", "08006", "08007", "08S01", "57P01"
    );

    private static boolean isTransient(SQLException ex) {
        String sqlState = ex.getSQLState();
        return sqlState != null && TRANSIENT_CODES.contains(sqlState);
    }

    private static void backoffDelayWithJitter(int inc) {
        try {
            TimeUnit.MILLISECONDS.sleep(
                    Math.min((long) (Math.pow(2, inc) + Math.random() * 1000), 5000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static final AtomicInteger monotonicId = new AtomicInteger();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    @Qualifier("simpleAsyncTaskExecutor")
    private AsyncTaskExecutor asyncTaskExecutor;

    Workload submitTask(String clusterId,
                        Duration duration,
                        Runnable task,
                        String description) {
        final Metrics metrics = Metrics.empty();

        final Workload workload = new Workload(clusterId,
                monotonicId.incrementAndGet(),
                description,
                metrics,
                duration);

        final Instant stopTime = Instant.now().plus(duration);

        final CompletableFuture<Void> future = asyncTaskExecutor.submitCompletable(() -> {
            final AtomicInteger retries = new AtomicInteger();

            logger.debug("Started workload: {}", workload);

            while (Instant.now().isBefore(stopTime)) {
                if (Thread.interrupted()) {
                    logger.warn("Thread interrupted - bailing out: {}", workload);
                    break;
                }

                final Instant callTime = Instant.now();

                try {
                    task.run();
                    retries.set(0);
                    metrics.markSuccess(Duration.between(callTime, Instant.now()));
                } catch (Exception ex) {
                    Duration callDuration = Duration.between(callTime, Instant.now());

                    Throwable cause = NestedExceptionUtils.getMostSpecificCause(ex);
                    boolean isTransient = false;

                    if (cause instanceof SQLException) {
                        String sqlState = ((SQLException) cause).getSQLState();

                        if (isTransient((SQLException) cause)) {
                            logger.debug("Transient SQL exception [%s]: [%s]".formatted(sqlState, cause));
                            isTransient = true;
                        } else {
                            logger.debug("Non-transient SQL exception [%s]: [%s]".formatted(sqlState, cause));
                        }
                    } else if (ex instanceof TransientDataAccessException) {
                        logger.debug("Transient data access exception: [%s]".formatted(ex));

                        isTransient = true;
                    } else if (ex instanceof NonTransientDataAccessException
                               || ex instanceof TransactionException) {
                        logger.debug("Non-transient exception: [%s]".formatted(ex));
                    } else {
                        throw new UndeclaredThrowableException(ex);
                    }

                    metrics.markFail(callDuration, isTransient);
                    workload.addProblem(Problem.of(ex).setTransient(isTransient));

                    backoffDelayWithJitter(retries.incrementAndGet());
                }
            }

            return null;
        });

        asyncTaskExecutor.submit(() -> {
            try {
                future.get();
                workload.setFailed(false);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                workload.setFailed(true).addProblem((Problem.of(e)));
            } catch (ExecutionException e) {
                workload.setFailed(true).addProblem(Problem.of(e.getCause()));
            } finally {
                logger.debug("Finished workload: {}", workload);
            }
        });

        workload.setFuture(future);

        return workload;
    }
}