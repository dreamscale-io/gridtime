package com.dreamscale.htmflow.core.feeds.story.grid;

import com.dreamscale.htmflow.core.domain.tile.FlowObjectType;
import com.dreamscale.htmflow.core.feeds.clock.GeometryClock;
import com.dreamscale.htmflow.core.feeds.clock.ZoomLevel;
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
public class StoryGridSummary {

    private double avgMood;

    private double percentLearning;
    private double percentTroubleshooting;
    private double percentProgress;

    private double percentPairing;

    private int boxesVisited;
    private int locationsVisited;
    private int traversalsVisited;
    private int bridgesVisited;
    private int bubblesVisited;

    private int totalExperiments;
    private int totalMessages;

}
