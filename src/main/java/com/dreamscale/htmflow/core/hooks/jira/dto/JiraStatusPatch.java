package com.dreamscale.htmflow.core.hooks.jira.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JiraStatusPatch {

    private JiraCommentUpdatePatch update;
    private JiraTransitionPatch transition;

    public JiraStatusPatch(String transitionId, String comment) {
        transition = new JiraTransitionPatch(transitionId);
        update = new JiraCommentUpdatePatch(comment);
    }
}
