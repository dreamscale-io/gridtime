package com.dreamscale.htmflow.core.feeds.story.feature.band;

import com.dreamscale.htmflow.core.domain.json.LinkedMember;
import lombok.Getter;

import java.util.List;

@Getter
public class AuthorsContext implements BandContext {

    private List<LinkedMember> pairedAuthors;

    public AuthorsContext(List<LinkedMember> pairedAuthors) {
        this.pairedAuthors = pairedAuthors;
    }
}
