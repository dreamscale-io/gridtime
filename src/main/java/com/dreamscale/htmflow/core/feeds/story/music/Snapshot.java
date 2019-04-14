package com.dreamscale.htmflow.core.feeds.story.music;

import com.dreamscale.htmflow.core.feeds.story.feature.context.Context;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.ExecuteThing;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.Box;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.Bridge;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.LocationInBox;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.Traversal;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class Snapshot {
    private final MusicGeometryClock.Coords coords;

    private Context projectContext;
    private Context taskContext;
    private Context intentionContext;

    private List<Box> activeBoxes;
    private List<LocationInBox> activeLocationsInBox;
    private List<Traversal> activeTraversals;
    private List<Bridge> activeBridges;
    private List<ExecuteThing> activeExecutionEvents;

    private Set<String> urisInFrame;

    public Snapshot(MusicGeometryClock.Coords coords) {
        this.coords = coords;
    }

}
