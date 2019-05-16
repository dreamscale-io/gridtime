package com.dreamscale.htmflow.core.feeds.story.feature.movement;

import com.dreamscale.htmflow.core.domain.tile.FlowObjectType;
import com.dreamscale.htmflow.core.feeds.story.music.MusicClock;
import com.dreamscale.htmflow.core.feeds.story.music.Playable;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public abstract class Movement extends FlowFeature implements Playable {


    private LocalDateTime moment;
    private int relativeSequence = 0;

    private MusicClock.Beat coordinates;

    public Movement(LocalDateTime moment, FlowObjectType flowObjectType) {
        super(flowObjectType);
        this.moment = moment;
    }

    public Movement(FlowObjectType flowObjectType) {
        super(flowObjectType);
    }


    public void initRelativeSequence(RhythmLayer layer, int nextSequence) {
        relativeSequence = nextSequence;

        setRelativePath("/movement/"+nextSequence);
        setUri(layer.getUri() + getRelativePath());
    }


}
