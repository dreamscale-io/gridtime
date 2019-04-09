package com.dreamscale.htmflow.core.feeds.story.feature.movement;

import java.time.LocalDateTime;

public class PostCircleMessage extends Movement {


    private final Message message;

    public PostCircleMessage(LocalDateTime moment, Message message) {
        super(moment, MovementType.POST_CIRCLE_MESSAGE, message);
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
