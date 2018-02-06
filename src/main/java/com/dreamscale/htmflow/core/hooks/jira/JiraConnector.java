package com.dreamscale.htmflow.core.hooks.jira;

import feign.auth.BasicAuthRequestInterceptor;
import org.dreamscale.feign.JacksonFeignBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
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

    public List<JiraProjectDto> getProjects() {
        return jiraClient.getProjects();
    }

    public JiraSearchResultPage getOpenTasksForProject(String projectId) {
        System.out.println(projectId);
        String jql = "project="+projectId+" and status not in (Done) order by updated desc";
        String fields = "id,key,summary";

        return jiraClient.getTasks(jql, fields);
    }

    public List<JiraUserDto> getUsers() {
        List<JiraUserDto> allUsers = jiraClient.getUsers();
        List<JiraUserDto> filteredUsers = new ArrayList<>();
        for (JiraUserDto user: allUsers) {
            if (!user.getKey().startsWith("addon_")) {
                filteredUsers.add(user);
            }
        }
        return filteredUsers;
    }

    JiraClient createJiraClient() {
        return jacksonFeignBuilder
                .requestInterceptor(new BasicAuthRequestInterceptor(user, apiKey))
                .target(JiraClient.class, siteUrl);
    }

}
