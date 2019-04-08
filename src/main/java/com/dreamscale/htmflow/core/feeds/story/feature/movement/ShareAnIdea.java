package com.dreamscale.htmflow.core.feeds.story.feature.movement;

import com.dreamscale.htmflow.core.feeds.story.feature.details.IdeaDetails;

import java.time.LocalDateTime;

public class ShareAnIdea extends Movement {


    private final IdeaDetails ideaDetails;

    public ShareAnIdea(LocalDateTime moment, IdeaDetails ideaDetails) {
        super(moment, MovementType.SHARE_AN_IDEA, null);
        this.ideaDetails = ideaDetails;
    }

}
