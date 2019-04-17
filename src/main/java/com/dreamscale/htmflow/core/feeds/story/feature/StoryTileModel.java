package com.dreamscale.htmflow.core.feeds.story.feature;

import com.dreamscale.htmflow.core.feeds.clock.GeometryClock;
import com.dreamscale.htmflow.core.feeds.clock.ZoomLevel;
import com.dreamscale.htmflow.core.feeds.story.feature.context.MomentOfContext;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.RhythmLayer;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.BoxAndBridgeActivity;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.TimebandLayer;
import com.dreamscale.htmflow.core.feeds.story.grid.FeatureAggregate;
import com.dreamscale.htmflow.core.feeds.story.grid.FeatureAggregateRow;
import com.dreamscale.htmflow.core.feeds.story.grid.FeatureRow;
import com.dreamscale.htmflow.core.feeds.story.grid.StoryGridModel;
import com.dreamscale.htmflow.core.feeds.story.music.Snapshot;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StoryTileModel extends FlowFeature {

    private String tileUri;
    private GeometryClock.Coords tileCoordinates;
    private ZoomLevel zoomLevel;

    private List<MomentOfContext> momentsOfContext;
    private List<TimebandLayer> bandLayers;
    private List<RhythmLayer> rhythmLayers;

    private BoxAndBridgeActivity spatialStructure;
    private StoryGridModel storyGridModel;

    private CarryOverContext carryOverContext;
}
