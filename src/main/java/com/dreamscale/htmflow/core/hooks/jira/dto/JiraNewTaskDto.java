package com.dreamscale.htmflow.core.hooks.jira.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class JiraNewTaskDto {

    private String summary;
    private String description;

    private String projectId;
    private String issueType;

}
