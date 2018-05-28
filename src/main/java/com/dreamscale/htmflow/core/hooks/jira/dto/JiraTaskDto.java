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
}
