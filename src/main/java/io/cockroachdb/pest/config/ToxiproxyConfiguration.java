package io.cockroachdb.pest.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.rekawek.toxiproxy.ToxiproxyClient;

import io.cockroachdb.pest.model.ApplicationSettings;

@Configuration
public class ToxiproxyConfiguration {
    @Autowired
    private ApplicationSettings applicationSettings;

    @Bean
    public ToxiproxyClient toxiproxyClient() {
        return new ToxiproxyClient(
                applicationSettings.getToxiproxy().getHost(),
                applicationSettings.getToxiproxy().getPort());
    }
}
