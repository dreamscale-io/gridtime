package com.dreamscale.htmflow.core.feeds.story.feature.movement;

import com.dreamscale.htmflow.core.feeds.story.feature.structure.Box;

import java.time.LocalDateTime;

public class MoveToBox extends Movement {

    private final Box box;

    public MoveToBox(LocalDateTime moment, Box box) {
        super(moment, MovementType.MOVE_TO_BOX, box);
        this.box = box;
    }

    public Box getBox() {
        return box;
    }
}
