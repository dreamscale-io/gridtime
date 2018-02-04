package com.dreamscale.htmflow.core.hooks.jira;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JiraProjectDetailsDto {

    String self;
    String id;
    String key;
    String name;
    Map<String, String> avatarUrls;
}
