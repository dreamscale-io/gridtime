package com.dreamscale.htmflow.core.feeds.story.feature.movement;

import com.dreamscale.htmflow.core.feeds.story.music.MusicGeometryClock;
import com.dreamscale.htmflow.core.feeds.story.music.Playable;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextSummary;

import java.time.LocalDateTime;

public class Movement extends FlowFeature implements Playable {

    private final MovementType type;
    private final FlowFeature staticReferenceObject;
    private LocalDateTime moment;
    private int relativeOffset = 0;

    private MusicGeometryClock.Coords coords;
    private ContextSummary context;

    public Movement(LocalDateTime moment, MovementType type, FlowFeature staticReferenceObject) {
        this.moment = moment;
        this.type = type;
        this.staticReferenceObject = staticReferenceObject;
    }

    public void setRelativeOffset(int nextSequence) {
        this.relativeOffset = nextSequence;
    }

    public void setCoordinates(MusicGeometryClock.Coords coords) {
        this.coords = coords;
    }

    public LocalDateTime getMoment() {
        return moment;
    }

    public int getRelativeOffset() {
        return relativeOffset;
    }

    public MusicGeometryClock.Coords getCoordinates() {
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

    public void setContext(ContextSummary context) {
        this.context = context;
    }

    public ContextSummary getContext() {
        return context;
    }
}
