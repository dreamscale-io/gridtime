package com.dreamscale.htmflow;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import com.dreamscale.htmflow.client.JournalClient;
import org.dreamscale.feign.JacksonFeignBuilder;
import org.dreamscale.test.BaseTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ComponentTestConfig extends BaseTestConfig {

    @Autowired
    JacksonFeignBuilder jacksonFeignBuilder;

    @Bean
    JournalClient contextClient() {
        return jacksonFeignBuilder.target(JournalClient.class, baseUrl);
    }

}
