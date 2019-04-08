package com.dreamscale.htmflow.core.feeds.story.feature.movement;

import com.dreamscale.htmflow.core.feeds.clock.InnerGeometryClock;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;

import java.time.LocalDateTime;

public class Movement extends FlowFeature {

    private final MovementType type;
    private final FlowFeature staticReferenceObject;
    private LocalDateTime moment;
    private int relativeOffset = 0;

    private InnerGeometryClock.Coords coords;

    public Movement(LocalDateTime moment, MovementType type, FlowFeature staticReferenceObject) {
        this.moment = moment;
        this.type = type;
        this.staticReferenceObject = staticReferenceObject;
    }

    public void setRelativeOffset(int nextSequence) {
        this.relativeOffset = nextSequence;
    }

    public void setCoordinates(InnerGeometryClock.Coords coords) {
        this.coords = coords;
    }

    public LocalDateTime getMoment() {
        return moment;
    }

    public int getRelativeOffset() {
        return relativeOffset;
    }

    public InnerGeometryClock.Coords getCoordinates() {
        return this.coords;
    }

    public MovementType getType() {
        return type;
    }

    public String getReferenceObjectPath() {
        String path = "";
        if (staticReferenceObject != null) {
            path = staticReferenceObject.getRelativePath();
        }

        return path;
    }
}
