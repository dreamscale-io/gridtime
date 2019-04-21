package com.dreamscale.htmflow.core.feeds.story.feature.movement;

import com.dreamscale.htmflow.core.feeds.story.music.MusicGeometryClock;
import com.dreamscale.htmflow.core.feeds.story.music.Playable;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.feature.context.MomentOfContext;

import java.time.LocalDateTime;

public class Movement extends FlowFeature implements Playable {

    private final MovementType type;
    private final FlowFeature staticReferenceObject;
    private LocalDateTime moment;
    private int relativeSequence = 0;

    private MusicGeometryClock.Coords coords;
    private MomentOfContext context;

    public Movement(LocalDateTime moment, MovementType type, FlowFeature staticReferenceObject) {
        this.moment = moment;
        this.type = type;
        this.staticReferenceObject = staticReferenceObject;
    }

    public void initRelativeSequence(RhythmLayer layer, int nextSequence) {
        relativeSequence = nextSequence;

        setRelativePath("/movement/"+nextSequence);
        setUri(layer.getUri() + getRelativePath());
    }

    public void setCoordinates(MusicGeometryClock.Coords coords) {
        this.coords = coords;
    }

    public LocalDateTime getMoment() {
        return moment;
    }

    public int getRelativeSequence() {
        return relativeSequence;
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

    public FlowFeature getReferenceObject() {
        return staticReferenceObject;
    }

    public void setContext(MomentOfContext context) {
        this.context = context;
    }

    public MomentOfContext getContext() {
        return context;
    }


}
