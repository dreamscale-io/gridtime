package com.dreamscale.ideaflow.core.hooks.jira.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JiraProjectDto {

    String expand;
    String self;
    String id;
    String key;
    String name;
    Map<String, String> avatarUrls;
    String projectTypeKey;
    boolean simplified;

}
