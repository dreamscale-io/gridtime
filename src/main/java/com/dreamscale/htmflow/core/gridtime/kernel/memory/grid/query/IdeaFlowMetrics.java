package com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.query;

import com.dreamscale.htmflow.core.gridtime.kernel.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.ZoomLevel;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.cache.FeatureCache;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.AuthorsReference;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.FeelsReference;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.IdeaFlowStateReference;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.MusicGrid;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.query.aggregate.AverageMetric;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.query.aggregate.MetricQuery;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.query.aggregate.PercentMetric;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.GridRow;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.type.GridCell;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.query.key.FeatureRowKey;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.type.AuthorsType;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.type.IdeaFlowStateType;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.Iterator;

@Getter
@Setter
public class IdeaFlowMetrics implements MetricQuery {

    private ZoomLevel zoomLevel;
    private IdeaFlowStateReference lastIdeaFlowState;

    private Duration timeInTile;
    private Double avgFlame;

    private Double percentWtf;
    private Double percentLearning;
    private Double percentProgress;
    private Double percentPairing;


    public static IdeaFlowMetrics queryFrom(FeatureCache featureCache, MusicClock musicClock, MusicGrid musicGrid) {


        Duration totalDuration = selectTotalDuration(musicClock);
        AverageMetric averageFlame = selectAverageFlame(musicGrid);

        PercentMetric percentWtfMetric = selectPercentWTF(featureCache, musicGrid);
        PercentMetric percentLearningAndProgressMetric = selectPercentLearningAndProgressNotInWtf(featureCache, musicClock, musicGrid);
        PercentMetric percentPairingMetric = selectPercentPairing(musicGrid);

        IdeaFlowMetrics metrics = new IdeaFlowMetrics();

        metrics.zoomLevel = musicClock.getZoomLevel();
        metrics.lastIdeaFlowState = selectLastIdeaFlowState(featureCache, musicGrid);
        metrics.timeInTile = totalDuration;
        metrics.avgFlame = averageFlame.getAverage();

        metrics.percentWtf = percentWtfMetric.getPercent("wtf");
        metrics.percentLearning = percentLearningAndProgressMetric.getPercent("learning");
        metrics.percentProgress = percentLearningAndProgressMetric.getPercent("progress");
        metrics.percentPairing = percentPairingMetric.getPercent("pairing");

        return metrics;
    }

    private static IdeaFlowStateReference selectLastIdeaFlowState(FeatureCache featureCache, MusicGrid musicGrid) {

        IdeaFlowStateReference wtfState = featureCache.lookupIdeaFlowStateReference(IdeaFlowStateType.WTF_STATE);
        IdeaFlowStateReference learningState = featureCache.lookupIdeaFlowStateReference(IdeaFlowStateType.LEARNING_STATE);
        IdeaFlowStateReference progressState = featureCache.lookupIdeaFlowStateReference(IdeaFlowStateType.PROGRESS_STATE);

        GridRow wtfRow = musicGrid.getRow(FeatureRowKey.FLOW_WTF);
        GridRow learningRow = musicGrid.getRow(FeatureRowKey.FLOW_LEARNING);

        GridCell wtfCell = wtfRow.getLast();
        GridCell learningCell = learningRow.getLast();

        IdeaFlowStateReference lastState;

        if (learningCell.hasFeature(learningState) && !wtfCell.hasFeature(wtfState)) {
            lastState = learningState;
        } else if (learningCell.hasFeature(progressState) && !wtfCell.hasFeature(wtfState)) {
            lastState = progressState;
        } else {
            lastState = wtfState;
        }
        return lastState;
    }

    private static Duration selectTotalDuration(MusicClock musicClock) {
        return musicClock.getRelativeEnd();
    }

    private static PercentMetric selectPercentWTF(FeatureCache featureCache, MusicGrid musicGrid) {
        GridRow row = musicGrid.getRow(FeatureRowKey.FLOW_WTF);

        IdeaFlowStateReference wtfState = featureCache.lookupIdeaFlowStateReference(IdeaFlowStateType.WTF_STATE);

        PercentMetric percentMetric = new PercentMetric();

        for (GridCell cell : row) {
            if (cell.hasFeature(wtfState)) {
                percentMetric.addSample("wtf");
            } else {
                percentMetric.addSample("other");
            }
        }

        percentMetric.calculatePercent("wtf");

        return percentMetric;
    }

    private static PercentMetric selectPercentLearningAndProgressNotInWtf(FeatureCache featureCache, MusicClock musicClock, MusicGrid musicGrid) {
        GridRow wtfRow = musicGrid.getRow(FeatureRowKey.FLOW_WTF);
        GridRow learningRow = musicGrid.getRow(FeatureRowKey.FLOW_LEARNING);

        IdeaFlowStateReference wtfState = featureCache.lookupIdeaFlowStateReference(IdeaFlowStateType.WTF_STATE);
        IdeaFlowStateReference learningState = featureCache.lookupIdeaFlowStateReference(IdeaFlowStateType.LEARNING_STATE);
        IdeaFlowStateReference progressState = featureCache.lookupIdeaFlowStateReference(IdeaFlowStateType.PROGRESS_STATE);

        PercentMetric percentMetric = new PercentMetric();

        Iterator<RelativeBeat> beatIterator = musicClock.getForwardsIterator();

        while (beatIterator.hasNext()) {
            RelativeBeat beat = beatIterator.next();

            GridCell wtfCell = wtfRow.getCell(beat);
            GridCell learningCell = learningRow.getCell(beat);

            if (learningCell.hasFeature(learningState) && !wtfCell.hasFeature(wtfState)) {
                percentMetric.addSample("learning");
            } else if (learningCell.hasFeature(progressState) && !wtfCell.hasFeature(wtfState)) {
                percentMetric.addSample("progress");
            } else {
                percentMetric.addSample("other");
            }
        }

        percentMetric.calculatePercent("learning");
        percentMetric.calculatePercent("progress");

        return percentMetric;
    }

    private static PercentMetric selectPercentPairing(MusicGrid musicGrid) {
        GridRow authorRow = musicGrid.getRow(FeatureRowKey.AUTHOR_NAME);

        PercentMetric percentMetric = new PercentMetric();

        for (GridCell cell : authorRow) {
            AuthorsReference authors = cell.getFeature();

            if (authors != null && (authors.getAuthorsType() == AuthorsType.PAIR
                    || authors.getAuthorsType() == AuthorsType.MOB)) {
                percentMetric.addSample("pairing");
            } else {
                percentMetric.addSample("other");
            }
        }
        percentMetric.calculatePercent("pairing");

        return percentMetric;
    }

    private static AverageMetric selectAverageFlame(MusicGrid musicGrid) {
        GridRow feelsRow = musicGrid.getRow(FeatureRowKey.FEELS_RATING);

        AverageMetric averageMetric = new AverageMetric();

        for (GridCell cell : feelsRow) {
            FeelsReference feels = cell.getFeature();

            if (feels != null) {
                averageMetric.addSample( feels.getFlameRating() );
            } else {
                averageMetric.addSample( 0 );
            }
        }
        return averageMetric;
    }

}
