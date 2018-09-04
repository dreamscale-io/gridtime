package com.dreamscale.htmflow.core.hooks.jira.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JiraTaskDto {

    String expand;
    String self;
    String id;
    String key;
    Map<String, Object> fields;

    private static final String JIRA_SUMMARY = "summary";
    private static final String JIRA_STATUS = "status";
    private static final String JIRA_STATUS_NAME = "name";

    private static final String JIRA_ASSIGNEE = "assignee";
    private static final String JIRA_ASSIGNEE_EMAIL = "emailAddress";



    public String getSummary() {
        String jiraSummary = null;
        if (getFields().get(JIRA_SUMMARY) != null) {
            jiraSummary = getFields().get(JIRA_SUMMARY).toString();
        }
        return jiraSummary;
    }

    public String getStatus() {
        String jiraStatus = null;
        if (getFields().get(JIRA_STATUS) != null) {
            jiraStatus = ((Map) getFields().get(JIRA_STATUS)).get(JIRA_STATUS_NAME).toString();
        }
        return jiraStatus;
    }

    public String getAssignee() {
        String assignee = null;
        if (getFields().get(JIRA_STATUS) != null) {
            assignee = ((Map) getFields().get(JIRA_ASSIGNEE)).get(JIRA_ASSIGNEE_EMAIL).toString();
        }
        return assignee;
    }

}
