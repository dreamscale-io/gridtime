package com.dreamscale.gridtime.core.hooks.jira.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JiraNewTaskDto {

    private String summary;
    private String description;

    private String projectId;
    private String issueType;

}
