package com.dreamscale.htmflow;

import com.dreamscale.htmflow.client.*;
import com.dreamscale.htmflow.core.domain.MasterAccountEntity;
import com.dreamscale.htmflow.core.hooks.jira.JiraConnection;
import com.dreamscale.htmflow.core.hooks.jira.JiraConnectionFactory;
import com.dreamscale.htmflow.core.security.AuthorizationRequestInterceptor;
import com.dreamscale.htmflow.core.security.MasterAccountIdResolver;
import com.dreamscale.htmflow.core.service.JiraService;
import org.dreamscale.feign.JacksonFeignBuilder;
import org.dreamscale.test.BaseTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import spock.mock.DetachedMockFactory;

import java.util.UUID;

@Configuration
public class ComponentTestConfig extends BaseTestConfig {

    @Autowired
    JacksonFeignBuilder jacksonFeignBuilder;

    @Bean
        DetachedMockFactory detachedMockFactory() {
        return new DetachedMockFactory();
    }

    @Bean
    @Primary
    public JiraService mockJiraService() {
        return detachedMockFactory().Mock(JiraService.class);
    }

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
    AdminClient adminClient() {
        return createClientWithStaticApiKey(jacksonFeignBuilder, AdminClient.class);
    }

    @Bean
    FlowClient publisherClient() {
        return createClientWithStaticApiKey(jacksonFeignBuilder, FlowClient.class);
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
