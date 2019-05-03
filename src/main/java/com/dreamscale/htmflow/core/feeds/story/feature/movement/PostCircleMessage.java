package com.dreamscale.htmflow.core.feeds.story.feature.movement;

import com.dreamscale.htmflow.core.domain.tile.FlowObjectType;
import com.dreamscale.htmflow.core.feeds.story.feature.details.MessageDetails;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class PostCircleMessage extends Movement {

    private MessageDetails messageDetails;

    public PostCircleMessage(LocalDateTime moment, MessageDetails messageDetails) {
        super(moment, FlowObjectType.MOVEMENT_POST_MESSAGE);
        this.messageDetails = messageDetails;
    }

    public PostCircleMessage() {
        super(FlowObjectType.MOVEMENT_POST_MESSAGE);
    }
}
