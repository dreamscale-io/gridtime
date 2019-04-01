package com.dreamscale.htmflow.core.feeds.story.feature.sequence;

import com.dreamscale.htmflow.core.feeds.clock.InnerGeometryClock;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;

import java.time.LocalDateTime;

public class Movement implements FlowFeature {

    private LocalDateTime moment;
    private final Object reference;
    private int relativeOffset = 0;

    private InnerGeometryClock.Coords coords;

    public Movement(LocalDateTime moment, Object reference) {
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
