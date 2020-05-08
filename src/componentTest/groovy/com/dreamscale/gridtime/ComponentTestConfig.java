package com.dreamscale.gridtime;

import com.dreamscale.gridtime.client.*;
import com.dreamscale.gridtime.core.CoreARandom;
import com.dreamscale.gridtime.core.CoreRandomBuilderSupport;
import com.dreamscale.gridtime.core.capability.integration.EmailCapability;
import com.dreamscale.gridtime.core.domain.member.RootAccountEntity;
import com.dreamscale.gridtime.core.security.AuthorizationRequestInterceptor;
import com.dreamscale.gridtime.core.security.RootAccountIdResolver;
import com.dreamscale.gridtime.core.capability.integration.JiraCapability;
import com.dreamscale.gridtime.core.service.GridClock;
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
    public JiraCapability mockJiraService() {
        return detachedMockFactory().Mock(JiraCapability.class);
    }

    @Bean
    @Primary
    public GridClock mockTimeService() {
        return detachedMockFactory().Mock(GridClock.class);
    }

    @Bean
    @Primary
    public EmailCapability mockEmailCapability() {
        return detachedMockFactory().Mock(EmailCapability.class);
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
    TalkToClient circuitTalkClient() {
        return createClientWithStaticApiKey(jacksonFeignBuilder, TalkToClient.class);
    }

    @Bean
    MemberClient memberStatusClient() {
        return createClientWithStaticApiKey(jacksonFeignBuilder, MemberClient.class);
    }

    @Bean
    SubscriptionClient subscriptionClient() {
        return createClientWithStaticApiKey(jacksonFeignBuilder, SubscriptionClient.class);
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
    LearningCircuitClient circuitClient() { return createClientWithStaticApiKey(jacksonFeignBuilder, LearningCircuitClient.class); }

    @Bean
    TeamCircuitClient teamCircuitClient() { return createClientWithStaticApiKey(jacksonFeignBuilder, TeamCircuitClient.class); }

    @Bean
    InviteToClient inviteToClient() { return createClientWithStaticApiKey(jacksonFeignBuilder, InviteToClient.class); }

    @Bean
    InvitationClient invitationClient() { return createClientWithStaticApiKey(jacksonFeignBuilder, InvitationClient.class); }


    AccountClient unauthenticatedAccountClient() {
        return jacksonFeignBuilder.target(AccountClient.class, baseUrl);
    }

    @Bean
    DictionaryClient dictionaryClient() {
        return createClientWithStaticApiKey(jacksonFeignBuilder, DictionaryClient.class);
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
    TeamClient teamClient() { return createClientWithStaticApiKey(jacksonFeignBuilder, TeamClient.class); }

    @Bean
    FlowClient unauthenticatedFlowClient() {
        return jacksonFeignBuilder.target(FlowClient.class, baseUrl);
    }

    @Bean
    public RootAccountEntity testUser() {
        return RootAccountEntity.builder()
                .id(UUID.randomUUID())
                .apiKey(UUID.randomUUID().toString())
                .rootEmail("janelle@dreamscale.io")
                .fullName("Janelle Klein")
                .build();
    }

    public <T> T createClientWithStaticApiKey(JacksonFeignBuilder builder, Class<T> clazz) {
        return builder.requestInterceptor(new AuthorizationRequestInterceptor(testUser()))
                .target(clazz, baseUrl);
    }

    @Bean
    @Primary
    public RootAccountIdResolver userIdResolver() {
        return new StubRootAccountIdResolver(testUser());
    }

    @Bean
    public CoreRandomBuilderSupport coreRandomBuilderSupport() {
        return CoreARandom.aRandom.coreRandomBuilderSupport;
    }


}
