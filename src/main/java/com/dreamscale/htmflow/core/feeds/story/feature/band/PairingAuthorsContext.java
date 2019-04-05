package com.dreamscale.htmflow.core.feeds.story.feature.band;

import com.dreamscale.htmflow.core.domain.json.LinkedMember;
import lombok.Getter;

import java.util.List;

@Getter
public class PairingAuthorsContext implements BandContext {

    private List<LinkedMember> pairedAuthors;

    public PairingAuthorsContext(List<LinkedMember> pairedAuthors) {
        this.pairedAuthors = pairedAuthors;
    }
}
