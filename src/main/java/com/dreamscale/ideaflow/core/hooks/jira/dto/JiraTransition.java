package com.dreamscale.ideaflow.core.hooks.jira.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JiraTransition {

    String id;
    String name;
    JiraStatus to;

    boolean hasScreen;
    boolean isGlobal;
    boolean isInitial;
    boolean isConditional;


}
