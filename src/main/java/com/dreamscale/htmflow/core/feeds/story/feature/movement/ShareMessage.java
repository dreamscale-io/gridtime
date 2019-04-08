package com.dreamscale.htmflow.core.feeds.story.feature.movement;

import com.dreamscale.htmflow.core.feeds.story.feature.details.IdeaDetails;

import java.time.LocalDateTime;

public class ShareMessage extends Movement {


    private final IdeaDetails ideaDetails;

    public ShareMessage(LocalDateTime moment, IdeaDetails ideaDetails) {
        super(moment, MovementType.SHARE_AN_IDEA, null);
        this.ideaDetails = ideaDetails;
    }

}
