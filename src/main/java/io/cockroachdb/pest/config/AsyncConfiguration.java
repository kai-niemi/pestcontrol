package io.cockroachdb.pest.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import org.springframework.web.context.request.async.TimeoutCallableProcessingInterceptor;

import io.cockroachdb.pest.ProfileNames;
import io.cockroachdb.pest.model.ApplicationProperties;

@Configuration
@EnableAsync
@EnableScheduling
@Profile(ProfileNames.WEB)
public class AsyncConfiguration implements AsyncConfigurer {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApplicationProperties applicationProperties;

    @Override
    public AsyncTaskExecutor getAsyncExecutor() {
        // Use a bounded thread pool with a blocking queue. When the queue is at
        // capacity (32 tasks), the thread pool increases to "maxPoolSize" threads.
        // Pool threads are also reclaimed when they are idle for 10 seconds.
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(applicationProperties.getPool().getThreadPoolMaxSize() <= 0
                ? Runtime.getRuntime().availableProcessors() : applicationProperties.getPool().getThreadPoolMaxSize());
        executor.setQueueCapacity(32);
        executor.setKeepAliveSeconds(10);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Bean(name = "simpleAsyncTaskExecutor")
    public SimpleAsyncTaskExecutor simpleAsyncTaskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setThreadNamePrefix("worker-");
        executor.setVirtualThreads(true);
        executor.setConcurrencyLimit(-1);
        return executor;
    }

    @Bean("applicationEventMulticaster")
    public ApplicationEventMulticaster simpleApplicationEventMulticaster(
            @Autowired @Qualifier("simpleAsyncTaskExecutor") Executor executor) {
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
        eventMulticaster.setTaskExecutor(executor);
        eventMulticaster.setErrorHandler(t -> {
            logger.error("Unexpected error occurred in scheduled task", t);
        });
        return eventMulticaster;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            logger.error("Unexpected exception occurred invoking async method: " + method, ex);
        };
    }

    @Bean
    public CallableProcessingInterceptor callableProcessingInterceptor() {
        return new TimeoutCallableProcessingInterceptor();
    }
}

