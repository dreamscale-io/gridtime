package com.dreamscale.htmflow;

import com.dreamscale.htmflow.client.AccountClient;
import com.dreamscale.htmflow.client.OrganizationClient;
import com.dreamscale.htmflow.client.ProjectClient;
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
    JournalClient journalClient() {
        return jacksonFeignBuilder.target(JournalClient.class, baseUrl);
    }

    @Bean
    OrganizationClient organizationClient() {
        return jacksonFeignBuilder.target(OrganizationClient.class, baseUrl);
    }

    @Bean
    ProjectClient projectClient() {
        return jacksonFeignBuilder.target(ProjectClient.class, baseUrl);
    }

    @Bean
    AccountClient accountClient() {
        return jacksonFeignBuilder.target(AccountClient.class, baseUrl);
    }
}
