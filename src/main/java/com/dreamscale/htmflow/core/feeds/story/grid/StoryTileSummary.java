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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class StoryTileSummary {

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


    public StoryTileSummary(StoryGridModel storyGrid) {

        setBoxesVisited(storyGrid.getFeatureMetricTotals().getBoxesVisited().size());
        setLocationsVisited(storyGrid.getFeatureMetricTotals().getLocationsVisited().size());
        setTraversalsVisited(storyGrid.getFeatureMetricTotals().getTraversalsVisited().size());
        setBridgesVisited(storyGrid.getFeatureMetricTotals().getBridgesVisited().size());
        setBubblesVisited(storyGrid.getFeatureMetricTotals().getBubblesVisited().size());

        double totalFeels = 0;
        double totalLearning = 0;
        double totalTroubleshooting = 0;
        double totalPairing = 0;

        Set<String> experimentUris = new LinkedHashSet<>();
        Set<String> messageUris = new LinkedHashSet<>();

        for (Column column : storyGrid.getColumns()) {
            totalFeels += column.getFeels();
            totalLearning += toIntFlag(column.isLearning());
            totalTroubleshooting += toIntFlag(column.isTroubleshooting());
            totalPairing += toIntFlag(column.isPairing());

            experimentUris.addAll(column.getExperimentContextUris());
            messageUris.addAll(column.getMessageContextUris());
        }

        int columnCount = storyGrid.getColumns().size();

        if (columnCount > 0) {
            setAvgMood(totalFeels / columnCount);
            setPercentLearning(totalLearning / columnCount);
            setPercentTroubleshooting(totalTroubleshooting / columnCount);
            setPercentPairing(totalPairing / columnCount);
        }

        setPercentProgress(1 - getPercentLearning() - getPercentTroubleshooting());

        setTotalExperiments(experimentUris.size());
        setTotalMessages(messageUris.size());
    }


    private int toIntFlag(boolean flag) {
        if (flag) {
            return 1;
        } else {
            return 0;
        }
    }

}
