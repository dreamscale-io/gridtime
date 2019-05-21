package com.dreamscale.htmflow.core.feeds.story;

import com.dreamscale.htmflow.core.feeds.story.feature.context.Context;
import com.dreamscale.htmflow.core.feeds.story.grid.Column;
import com.dreamscale.htmflow.core.feeds.story.grid.TileGridModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class StoryTileSummary {

    private Context lastProject;
    private Context lastTask;
    private Context lastIntention;

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


    public StoryTileSummary(TileGridModel tileGrid) {

        configureContexts(tileGrid.getLastColumn());


        setBoxesVisited(tileGrid.getFeatureMetricTotals().getBoxesVisited().size());
        setLocationsVisited(tileGrid.getFeatureMetricTotals().getLocationsVisited().size());
        setTraversalsVisited(tileGrid.getFeatureMetricTotals().getTraversalsVisited().size());
        setBridgesVisited(tileGrid.getFeatureMetricTotals().getBridgesVisited().size());
        setBubblesVisited(tileGrid.getFeatureMetricTotals().getBubblesVisited().size());

        double totalFeels = 0;
        double totalLearning = 0;
        double totalTroubleshooting = 0;
        double totalPairing = 0;

        Set<String> experimentUris = new LinkedHashSet<>();
        Set<String> messageUris = new LinkedHashSet<>();

        for (Column column : tileGrid.getColumns()) {
            totalFeels += column.getFeels();
            totalLearning += toIntFlag(column.isLearning());
            totalTroubleshooting += toIntFlag(column.isTroubleshooting());
            totalPairing += toIntFlag(column.isPairing());

            experimentUris.addAll(column.getExperimentContextUris());
            messageUris.addAll(column.getMessageContextUris());
        }

        int columnCount = tileGrid.getColumns().size();

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

    private void configureContexts(Column lastColumn) {
        if (lastColumn != null) {
            setLastProject(lastColumn.getProjectContext());
            setLastTask(lastColumn.getTaskContext());
            setLastIntention(lastColumn.getIntentionContext());
        }
    }

    public String toString() {
        return lastProject + ":" + lastTask;
    }

    private int toIntFlag(boolean flag) {
        if (flag) {
            return 1;
        } else {
            return 0;
        }
    }

}
