package com.dreamscale.htmflow.core.feeds.story;

import com.dreamscale.htmflow.core.domain.tile.FlowObjectType;
import com.dreamscale.htmflow.core.feeds.clock.GeometryClock;
import com.dreamscale.htmflow.core.feeds.clock.ZoomLevel;
import com.dreamscale.htmflow.core.feeds.story.feature.CarryOverContext;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.feature.context.MomentOfContext;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.RhythmLayer;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.BoxAndBridgeActivity;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.TimebandLayer;
import com.dreamscale.htmflow.core.feeds.story.grid.StoryGridModel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StoryTileModel extends FlowFeature {

    private String tileUri;
    private GeometryClock.StoryCoords tileCoordinates;
    private ZoomLevel zoomLevel;

    private List<MomentOfContext> momentsOfContext;
    private List<TimebandLayer> bandLayers;
    private List<RhythmLayer> rhythmLayers;
    private BoxAndBridgeActivity spatialStructure;

    private StoryGridModel storyGridModel;

    private CarryOverContext carryOverContext;

    public StoryTileModel() {
        super(FlowObjectType.STORY_TILE);
    }
}
