package io.cockroachdb.pest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

@Configuration
public class JacksonConfiguration {
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES) // strict
                .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
                .setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY);
    }

    @Bean
    public ObjectMapper yamlObjectMapper() {
        YAMLFactory yamlFactory = new YAMLFactory()
                .enable(YAMLGenerator.Feature.ALLOW_LONG_KEYS)
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                .disable(YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS);

        return new ObjectMapper(yamlFactory)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES) // strict
                .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
                .setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY);
    }

}
