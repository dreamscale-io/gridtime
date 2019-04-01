package com.dreamscale.htmflow.core.hooks.jira.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JiraCommentBody {

    String body;

}
