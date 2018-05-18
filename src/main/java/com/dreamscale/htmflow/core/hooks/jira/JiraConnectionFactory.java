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

        String formattedSiteUrl = formatSiteUrl(siteUrl);

        JiraClient jiraClient = jacksonFeignBuilder
                .requestInterceptor(new BasicAuthRequestInterceptor(user, apiKey))
                .target(JiraClient.class, formattedSiteUrl);

        JiraConnection connection = new JiraConnection(jiraClient);

        return connection;
    }

    private String formatSiteUrl(String jiraSiteUrl) {
        String siteUrl;
        if (jiraSiteUrl.contains("//")) {
            String afterProtocol = jiraSiteUrl.substring(jiraSiteUrl.indexOf("//") + 1);
            siteUrl = "https://" + afterProtocol;
        } else {
            siteUrl = "https://" + jiraSiteUrl;
        }
        return siteUrl;
    }

}
