package com.dreamscale.gridtime.core.hooks.jira.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JiraStatus {

    String self;
    String description;
    String iconUrl;
    String name;
    String id;
    JiraStatusCategory statusCategory;

}
