package io.cockroachdb.pc.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.DefaultCurieProvider;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsConfiguration;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsOptions;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.pattern.PathPatternParser;

import eu.rekawek.toxiproxy.model.ToxicDirection;
import eu.rekawek.toxiproxy.model.ToxicType;
import io.cockroachdb.pc.workload.profile.WorkloadType;
import io.cockroachdb.pc.web.api.toxi.ToxicForm;
import io.cockroachdb.pc.web.api.workload.WorkloadForm;
import io.cockroachdb.pc.web.api.LinkRelations;

@EnableWebMvc
@EnableHypermediaSupport(type = {
        EnableHypermediaSupport.HypermediaType.HAL_FORMS,
        EnableHypermediaSupport.HypermediaType.HAL
})
@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    @Autowired
    @Qualifier("simpleAsyncTaskExecutor")
    private AsyncTaskExecutor taskExecutor;

    @Autowired
    private CallableProcessingInterceptor callableProcessingInterceptor;

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setPatternParser(new PathPatternParser());
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(taskExecutor);
        configurer.registerCallableInterceptors(callableProcessingInterceptor);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedOrigins("*")
                .allowedHeaders("*");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("/webjars/");
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");

        if (!registry.hasMappingForPattern("/browser/**")) {
            registry.addResourceHandler("/browser/**").addResourceLocations(
                    "/webjars/hal-explorer/1.2.3/");
        }
    }

    @Bean
    public CurieProvider defaultCurieProvider() {
        return new DefaultCurieProvider(LinkRelations.CURIE_NAMESPACE,
                UriTemplate.of("/api/rels/{rel}"));
    }

    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

    @Bean
    public HalFormsConfiguration halFormsConfiguration() {
        return new HalFormsConfiguration()
                .withOptions(WorkloadForm.class, "workloadType", metadata ->
                        HalFormsOptions.inline(WorkloadType.values()))
                .withOptions(ToxicForm.class, "toxicType", metadata ->
                        HalFormsOptions.inline(ToxicType.values()))
                .withOptions(ToxicForm.class, "toxicDirection", metadata ->
                        HalFormsOptions.inline(ToxicDirection.values()));
    }
}
