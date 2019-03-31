package com.dreamscale.ideaflow;

import com.dreamscale.ideaflow.client.*;
import com.dreamscale.ideaflow.core.CoreARandom;
import com.dreamscale.ideaflow.core.CoreRandomBuilderSupport;
import com.dreamscale.ideaflow.core.domain.MasterAccountEntity;
import com.dreamscale.ideaflow.core.security.AuthorizationRequestInterceptor;
import com.dreamscale.ideaflow.core.security.MasterAccountIdResolver;
import com.dreamscale.ideaflow.core.service.JiraService;
import com.dreamscale.ideaflow.core.service.TimeService;
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
    @Primary
    public TimeService mockTimeService() {
        return detachedMockFactory().Mock(TimeService.class);
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
    MemberStatusClient memberStatusClient() {
        return createClientWithStaticApiKey(jacksonFeignBuilder, MemberStatusClient.class);
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
    CircleClient circleClient() { return createClientWithStaticApiKey(jacksonFeignBuilder, CircleClient.class); }

    AccountClient unauthenticatedAccountClient() {
        return jacksonFeignBuilder.target(AccountClient.class, baseUrl);
    }

    @Bean
    FlowClient flowClient() {
        return createClientWithStaticApiKey(jacksonFeignBuilder, FlowClient.class);
    }

    @Bean
    SpiritClient spiritClient() {
        return createClientWithStaticApiKey(jacksonFeignBuilder, SpiritClient.class);
    }

    @Bean
    FlowClient unauthenticatedFlowClient() {
        return jacksonFeignBuilder.target(FlowClient.class, baseUrl);
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

    @Bean
    public CoreRandomBuilderSupport coreRandomBuilderSupport() {
        return CoreARandom.aRandom.coreRandomBuilderSupport;
    }


}