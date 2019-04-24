package com.dreamscale.htmflow.core.hooks.hypercore;

import com.dreamscale.htmflow.core.hooks.jira.JiraClient;
import com.dreamscale.htmflow.core.hooks.jira.JiraConnection;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.auth.BasicAuthRequestInterceptor;
import org.dreamscale.feign.JacksonFeignBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HypercoreConnectionFactory {

    @Autowired
    private JacksonFeignBuilder jacksonFeignBuilder;

    private static final String HYPERCORE_SERVER_URI = "https://hypercore.dreamscale.io/api";

    public HypercoreConnection connect(String publicKey, String secretKey) {

        HypercoreClient hypercoreClient = jacksonFeignBuilder
                .requestInterceptor(new AuthInterceptor(publicKey, secretKey))
                .target(HypercoreClient.class, HYPERCORE_SERVER_URI);

        return new HypercoreConnection(hypercoreClient);
    }

    public HypercoreConnection connect() {

        HypercoreClient hypercoreClient = jacksonFeignBuilder
                .target(HypercoreClient.class, HYPERCORE_SERVER_URI);

        return new HypercoreConnection(hypercoreClient);
    }

    private class AuthInterceptor implements RequestInterceptor {

        private final String publicKey;
        private final String secretKey;

        AuthInterceptor(String publicKey, String secretKey) {
            this.publicKey = publicKey;
            this.secretKey = secretKey;
        }
        @Override
        public void apply(RequestTemplate template) {
            template.header("x-api-public-key", publicKey);
            template.header("x-api-secret-key", secretKey);
        }
    }


}
