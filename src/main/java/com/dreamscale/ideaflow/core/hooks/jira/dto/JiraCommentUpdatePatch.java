package com.dreamscale.ideaflow.core.hooks.jira.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JiraCommentUpdatePatch {

    List<JiraComment> comment;

    public JiraCommentUpdatePatch(String commentStr) {
        comment = new ArrayList<JiraComment>();
        comment.add(new JiraComment(commentStr));
    }
}
