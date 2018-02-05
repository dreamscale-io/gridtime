package com.dreamscale.htmflow.core.hooks.jira;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JiraSearchResultPage {

    String expand;
    Integer startAt;
    Integer maxResults;
    Integer total;

    List<JiraTaskDto> issues;
}
