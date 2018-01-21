package com.dreamscale.htmflow;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import com.dreamscale.htmflow.client.JournalClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ComponentTestConfig {

    @Value("http://localhost")
    protected String serverBaseUrl;
    @Value("http://localhost:${server.port}")
    protected String hostUri;

    @Bean
    JournalClient contextClient() {
        return Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .target(JournalClient.class, hostUri);
    }

}
