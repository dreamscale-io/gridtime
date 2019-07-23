package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.analytics.query;

import com.dreamscale.htmflow.core.gridtime.machine.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.machine.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.machine.clock.ZoomLevel;
import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.FeatureCache;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference.AuthorsReference;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference.FeelsReference;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference.IdeaFlowStateReference;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.AggregateGrid;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.IMusicGrid;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.query.aggregate.AverageMetric;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.query.aggregate.MetricQuery;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.query.aggregate.PercentMetric;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.GridRow;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.type.GridCell;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.query.key.FeatureRowKey;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.query.key.MetricRowKey;
import com.dreamscale.htmflow.core.gridtime.machine.memory.type.AuthorsType;
import com.dreamscale.htmflow.core.gridtime.machine.memory.type.IdeaFlowStateType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.util.Iterator;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IdeaFlowMetrics implements MetricQuery {

    private ZoomLevel zoomLevel;

    private Duration timeInTile;
    private Double avgFlame;

    private Double percentWtf;
    private Double percentLearning;
    private Double percentProgress;
    private Double percentPairing;




    public static IdeaFlowMetrics queryFrom(FeatureCache featureCache, MusicClock musicClock, IMusicGrid musicGrid) {


        Duration totalDuration = selectTotalDuration(musicClock);
        AverageMetric averageFlame = selectAverageFlame(musicGrid);

        PercentMetric percentWtfMetric = selectPercentWTF(featureCache, musicGrid);
        PercentMetric percentLearningAndProgressMetric = selectPercentLearningAndProgressNotInWtf(featureCache, musicClock, musicGrid);
        PercentMetric percentPairingMetric = selectPercentPairing(musicGrid);

        IdeaFlowMetrics metrics = new IdeaFlowMetrics();

        metrics.zoomLevel = musicClock.getZoomLevel();
        metrics.timeInTile = totalDuration;
        metrics.avgFlame = averageFlame.getAverage();

        metrics.percentWtf = percentWtfMetric.getPercent("wtf");
        metrics.percentLearning = percentLearningAndProgressMetric.getPercent("learning");
        metrics.percentProgress = percentLearningAndProgressMetric.getPercent("progress");
        metrics.percentPairing = percentPairingMetric.getPercent("pairing");

        return metrics;
    }

//    public static IdeaFlowMetrics queryFrom(AggregateGrid aggregateGrid) {
//        aggregateGrid.getTilePropertyMetric()
//
//    }

    public Map<MetricRowKey, Object> toProps() {
        Map<MetricRowKey, Object> props = DefaultCollections.map();

        props.put(MetricRowKey.TIME_IN_TILE, timeInTile);
        props.put(MetricRowKey.AVG_FLAME, avgFlame);
        props.put(MetricRowKey.PERCENT_WTF, percentWtf);
        props.put(MetricRowKey.PERCENT_LEARNING, percentLearning);
        props.put(MetricRowKey.PERCENT_PROGRESS, percentProgress);
        props.put(MetricRowKey.PERCENT_PAIRING, percentPairing);

        return props;
    }

    public static IdeaFlowMetrics fromProps(ZoomLevel zoomLevel, Map<MetricRowKey, Object> props) {
        IdeaFlowMetrics metrics = new IdeaFlowMetrics();

        metrics.zoomLevel = zoomLevel;
        metrics.timeInTile = (Duration) props.get(MetricRowKey.TIME_IN_TILE);
        metrics.avgFlame = (Double) props.get(MetricRowKey.AVG_FLAME);
        metrics.percentWtf = (Double) props.get(MetricRowKey.PERCENT_WTF);
        metrics.percentLearning = (Double) props.get(MetricRowKey.PERCENT_LEARNING);
        metrics.percentProgress = (Double) props.get(MetricRowKey.PERCENT_PROGRESS);
        metrics.percentPairing = (Double) props.get(MetricRowKey.PERCENT_PAIRING);

        return metrics;

    }


    private static Duration selectTotalDuration(MusicClock musicClock) {
        return musicClock.getRelativeEnd();
    }

    private static PercentMetric selectPercentWTF(FeatureCache featureCache, IMusicGrid musicGrid) {
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

    private static PercentMetric selectPercentLearningAndProgressNotInWtf(FeatureCache featureCache, MusicClock musicClock, IMusicGrid musicGrid) {
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

    private static PercentMetric selectPercentPairing(IMusicGrid musicGrid) {
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

    private static AverageMetric selectAverageFlame(IMusicGrid musicGrid) {
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
