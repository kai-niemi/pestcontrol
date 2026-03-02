package io.cockroachdb.pest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.cockroachdb.pest.ProfileNames;

@Configuration
@EnableScheduling
@Profile(ProfileNames.ONLINE)
public class AsyncConfiguration {
    @Bean
    @Primary
    public AsyncTaskExecutor backgroundTaskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setThreadNamePrefix("background-task-");
        executor.setCancelRemainingTasksOnClose(true);
        executor.setVirtualThreads(true);
        executor.setConcurrencyLimit(-1);
        return executor;
    }
}

