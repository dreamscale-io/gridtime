package com.dreamscale.gridtime.core.hooks.jira.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JiraStatusCategory {

    String self;
    Integer id;
    String colorName;
    String name;

}
