package com.dreamscale.htmflow.core.feeds.story.feature.details;

import com.dreamscale.htmflow.core.domain.member.json.LinkedMember;
import lombok.Getter;

import java.util.List;

@Getter
public class AuthorDetails implements Details {

    private List<LinkedMember> pairedAuthors;

    public AuthorDetails(List<LinkedMember> pairedAuthors) {
        this.pairedAuthors = pairedAuthors;
    }
}
