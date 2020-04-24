package com.dreamscale.gridtime.api.organization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JiraConfigDto {

    private String jiraSiteUrl;
    private String jiraUser;
    private String jiraApiKey;
}
