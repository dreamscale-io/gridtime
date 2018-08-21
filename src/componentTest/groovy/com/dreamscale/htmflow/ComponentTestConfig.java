package com.dreamscale.htmflow;

import com.dreamscale.htmflow.client.AccountClient;
import com.dreamscale.htmflow.client.OrganizationClient;
import com.dreamscale.htmflow.client.ProjectClient;
import com.dreamscale.htmflow.client.PublisherClient;
import com.dreamscale.htmflow.core.domain.MasterAccountEntity;
import com.dreamscale.htmflow.core.security.AuthorizationRequestInterceptor;
import com.dreamscale.htmflow.core.security.MasterAccountIdResolver;
import com.dreamscale.htmflow.client.JournalClient;
import org.dreamscale.feign.JacksonFeignBuilder;
import org.dreamscale.test.BaseTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.UUID;

@Configuration
public class ComponentTestConfig extends BaseTestConfig {

    @Autowired
    JacksonFeignBuilder jacksonFeignBuilder;

    @Bean
    JournalClient journalClient() {
        return createClientWithStaticApiKey(jacksonFeignBuilder, JournalClient.class);
    }

    @Bean
    OrganizationClient organizationClient() {
        return createClientWithStaticApiKey(jacksonFeignBuilder, OrganizationClient.class);
    }

    @Bean
    ProjectClient projectClient() {
        return createClientWithStaticApiKey(jacksonFeignBuilder, ProjectClient.class);
    }

    @Bean
    AccountClient accountClient() {
        return createClientWithStaticApiKey(jacksonFeignBuilder, AccountClient.class);
    }

    @Bean
    PublisherClient publisherClient() {
        return createClientWithStaticApiKey(jacksonFeignBuilder, PublisherClient.class);
    }

    @Bean
    public MasterAccountEntity testUser() {
        return MasterAccountEntity.builder()
                .id(UUID.randomUUID())
                .apiKey(UUID.randomUUID().toString())
                .masterEmail("janelle@dreamscale.io")
                .fullName("Janelle Klein")
                .build();
    }

    public <T> T createClientWithStaticApiKey(JacksonFeignBuilder builder, Class<T> clazz) {
        return builder.requestInterceptor(new AuthorizationRequestInterceptor(testUser()))
                .target(clazz, baseUrl);
    }

    @Bean
    @Primary
    public MasterAccountIdResolver userIdResolver() {
        return new StubMasterAccountIdResolver(testUser());
    }

}
