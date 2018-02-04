package com.dreamscale.htmflow.core.hooks.jira;

import org.dreamscale.feign.JacksonFeignBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class JiraConnector {

    @Value("${jira.site-url}")
    private String siteUrl;

    @Value("${jira.user}")
    private String user;

    @Value("${jira.api-key}")
    private String apiKey;

    @Autowired
    JacksonFeignBuilder jacksonFeignBuilder;

    JiraClient jiraClient;

    @PostConstruct
    void init() {
        jiraClient = createJiraClient();
    }

    List<JiraProjectDto> getProjects() {
        return jiraClient.getProjects();
    }

    JiraClient createJiraClient() {
        return jacksonFeignBuilder.target(JiraClient.class, siteUrl);
    }

}
