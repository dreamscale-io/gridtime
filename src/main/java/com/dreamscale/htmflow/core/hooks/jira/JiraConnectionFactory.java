package com.dreamscale.htmflow.core.hooks.jira;

import com.dreamscale.htmflow.core.domain.OrganizationEntity;
import feign.auth.BasicAuthRequestInterceptor;
import org.dreamscale.feign.JacksonFeignBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JiraConnectionFactory {

    @Autowired
    private JacksonFeignBuilder jacksonFeignBuilder;

    public JiraConnection connect(String siteUrl, String user, String apiKey) {

        JiraClient jiraClient = jacksonFeignBuilder
                .requestInterceptor(new BasicAuthRequestInterceptor(user, apiKey))
                .target(JiraClient.class, siteUrl);

        JiraConnection connection = new JiraConnection(jiraClient);

        connection.validate();

        return connection;
    }

}
