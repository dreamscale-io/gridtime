package com.dreamscale.gridtime.core.hooks.jira.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JiraComment {

    JiraCommentBody add;

    public JiraComment(String commentStr) {
        add = new JiraCommentBody(commentStr);
    }
}
