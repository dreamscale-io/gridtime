package com.dreamscale.htmflow;

import com.dreamscale.htmflow.client.AccountClient;
import com.dreamscale.htmflow.client.OrganizationClient;
import com.dreamscale.htmflow.client.ProjectClient;
import com.dreamscale.htmflow.core.domain.MasterAccountEntity;
import com.dreamscale.htmflow.core.security.AuthorizationRequestInterceptor;
import com.dreamscale.htmflow.core.security.UserIdResolver;
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
    public MasterAccountEntity testUser() {
        return MasterAccountEntity.builder()
                .id(UUID.randomUUID())
                .apiKey(UUID.randomUUID().toString())
                .masterEmail("janelle@dreamscale.io")
                .fullName("Janelle Klein")
                .build();
    }

    public <T> T createClientWithStaticApiKey(JacksonFeignBuilder builder, Class<T> clazz) {
        String apiKey = testUser().getApiKey();

        return builder.requestInterceptor(new AuthorizationRequestInterceptor(apiKey))
                .target(clazz, baseUrl);
    }

    @Bean
    @Primary
    public UserIdResolver userIdResolver() {
        return new StubUserIdResolver(testUser());
    }

    private static class StubUserIdResolver implements UserIdResolver {

        private MasterAccountEntity user;

        public StubUserIdResolver(MasterAccountEntity user) {
            System.out.println("STUB CREATED!!!");
            this.user = user;
        }

        @Override
        public UUID findAccountIdByApiKey(String apiKey) {
            return user.getApiKey().equals(apiKey) ? user.getId() : null;
        }

    }
}
