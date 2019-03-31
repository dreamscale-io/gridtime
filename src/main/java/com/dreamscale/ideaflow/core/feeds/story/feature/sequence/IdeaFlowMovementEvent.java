package com.dreamscale.ideaflow.core.feeds.story.feature.sequence;

import com.dreamscale.ideaflow.core.feeds.clock.InnerGeometryClock;
import com.dreamscale.ideaflow.core.feeds.story.feature.IdeaFlowFeature;

import java.time.LocalDateTime;

public class IdeaFlowMovementEvent implements IdeaFlowFeature {

    private LocalDateTime moment;
    private final Object reference;
    private int relativeOffset = 0;

    private InnerGeometryClock.Coords coords;

    public IdeaFlowMovementEvent(LocalDateTime moment, Object reference) {
        this.moment = moment;
        this.reference = reference;
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

    public Object getReference() {
        return reference;
    }

    public int getRelativeOffset() {
        return relativeOffset;
    }

    public boolean referencesType(Class<?> referenceObjectType) {
        return (reference != null && reference.getClass().equals(referenceObjectType));
    }


    public InnerGeometryClock.Coords getCoordinates() {
        return this.coords;
    }
}
