package com.dreamscale.ideaflow.api.organization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationInputDto {
    private String orgName;
    private String domainName;

    private String jiraSiteUrl;
    private String jiraUser;
    private String jiraApiKey;
}
