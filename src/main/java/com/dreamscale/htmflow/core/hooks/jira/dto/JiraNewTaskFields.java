package com.dreamscale.htmflow.core.hooks.jira.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JiraNewTaskFields {

    private Map<String, Object> fields = new HashMap<>();

    public void addIssueType(String issueType) {
        fields.put("issuetype", new JiraNewTaskIssueTypeField(issueType));
    }

    public void addSummary(String summary) {
        fields.put("summary", summary);
    }

    public void addDescription(String description) {
        fields.put("description", description);
    }

    public void addProject(String projectId) {
        fields.put("project", new JiraNewTaskProjectField(projectId));
    }

}
