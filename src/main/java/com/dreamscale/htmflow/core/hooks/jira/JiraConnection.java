package com.dreamscale.htmflow.core.hooks.jira;

import org.dreamscale.exception.WebApplicationException;

import java.util.ArrayList;
import java.util.List;

public class JiraConnection {

    private final JiraClient jiraClient;

    public JiraConnection(JiraClient jiraClient) {
        this.jiraClient = jiraClient;
    }

    public List<JiraProjectDto> getProjects() {
        return jiraClient.getProjects();
    }

    public List<JiraTaskDto> getOpenTasksForProject(String projectId) {
        System.out.println(projectId);
        String jql = "project="+projectId+" and status not in (Done) order by updated desc";
        String fields = "id,key,summary";

        JiraSearchResultPage searchResultPage = jiraClient.getTasks(jql, fields);
        return searchResultPage.getIssues();
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

    public void validate() {
        try {
            List<JiraProjectDto> projects = jiraClient.getProjects();
        } catch (WebApplicationException ex) {
            throw new JiraException("["+ex.getStatusCode() + "]: Unable to retrieve data from Jira, "+ex.getMessage(), ex);
        }
    }
}
