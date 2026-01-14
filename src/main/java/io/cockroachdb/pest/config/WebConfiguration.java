package io.cockroachdb.pest.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.cockroachdb.pest.ProfileNames;

@Configuration
@Profile(ProfileNames.WEB)
public class WebConfiguration implements WebMvcConfigurer {
    @Autowired
    @Qualifier("simpleAsyncTaskExecutor")
    private AsyncTaskExecutor taskExecutor;

    @Autowired
    private CallableProcessingInterceptor callableProcessingInterceptor;

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(taskExecutor);
        configurer.registerCallableInterceptors(callableProcessingInterceptor);
    }
}
