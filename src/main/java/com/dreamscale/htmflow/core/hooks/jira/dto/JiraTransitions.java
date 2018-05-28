package com.dreamscale.htmflow.core.hooks.jira.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JiraTransitions {

    String expand;
    List<JiraTransition> transitions;

}
