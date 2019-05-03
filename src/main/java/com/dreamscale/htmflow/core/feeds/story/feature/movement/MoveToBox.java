package com.dreamscale.htmflow.core.feeds.story.feature.movement;

import com.dreamscale.htmflow.core.domain.tile.FlowObjectType;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.Box;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class MoveToBox extends Movement {

    private Box box;

    public MoveToBox(LocalDateTime moment, Box box) {
        super(moment, FlowObjectType.MOVEMENT_TO_BOX);
        this.box = box;
    }

    public MoveToBox() {
        super(FlowObjectType.MOVEMENT_TO_BOX);
    }

    public Box getBox() {
        return box;
    }
}
