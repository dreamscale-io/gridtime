package com.dreamscale.htmflow.core.feeds.story.feature.movement;

import com.dreamscale.htmflow.core.feeds.story.feature.details.MessageDetails;

import java.time.LocalDateTime;

public class PostMessageToCircle extends Movement {


    private final MessageDetails messageDetails;

    public PostMessageToCircle(LocalDateTime moment, MessageDetails messageDetails) {
        super(moment);
        this.messageDetails = messageDetails;
    }

}
