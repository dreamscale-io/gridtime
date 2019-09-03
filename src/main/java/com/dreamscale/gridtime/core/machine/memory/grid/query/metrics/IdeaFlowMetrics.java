package com.dreamscale.gridtime.core.machine.memory.grid.query.metrics;

import com.dreamscale.gridtime.core.machine.clock.MusicClock;
import com.dreamscale.gridtime.core.machine.clock.RelativeBeat;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.AuthorsReference;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeelsReference;
import com.dreamscale.gridtime.core.machine.memory.grid.IMusicGrid;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.AggregateType;
import com.dreamscale.gridtime.core.machine.memory.grid.query.aggregate.AverageMetric;
import com.dreamscale.gridtime.core.machine.memory.grid.query.aggregate.MetricQuery;
import com.dreamscale.gridtime.core.machine.memory.grid.query.aggregate.PercentMetric;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.GridRow;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.type.GridCell;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.FeatureRowKey;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.MetricRowKey;
import com.dreamscale.gridtime.core.machine.memory.type.AuthorsType;
import com.dreamscale.gridtime.core.machine.memory.type.IdeaFlowStateType;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Iterator;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Slf4j
public class IdeaFlowMetrics implements MetricQuery {

    private ZoomLevel zoomLevel;

    private Duration timeInTile;
    private Double avgFlame;

    private Double percentWtf;
    private Double percentLearning;
    private Double percentProgress;
    private Double percentPairing;

//do I put gridTime in here?  Then when I load up these objects, maybe I use a view, and get the grid times.

    public static IdeaFlowMetrics queryFrom(IMusicGrid aggregateGrid) {

        IdeaFlowMetrics metrics = new IdeaFlowMetrics();

        metrics.zoomLevel = aggregateGrid.getZoomLevel();
        metrics.timeInTile = aggregateGrid.getTotalDuration();

        metrics.avgFlame = getSummaryMetricValue(aggregateGrid, MetricRowKey.ZOOM_AVG_FLAME, AggregateType.AVG);

        metrics.percentWtf = getSummaryMetricValue(aggregateGrid, MetricRowKey.ZOOM_PERCENT_WTF, AggregateType.AVG);
        metrics.percentLearning = getSummaryMetricValue(aggregateGrid, MetricRowKey.ZOOM_PERCENT_LEARNING, AggregateType.AVG);
        metrics.percentProgress = getSummaryMetricValue(aggregateGrid, MetricRowKey.ZOOM_PERCENT_PROGRESS, AggregateType.AVG);
        metrics.percentPairing = getSummaryMetricValue(aggregateGrid, MetricRowKey.ZOOM_PERCENT_PAIRING, AggregateType.AVG);

        return metrics;
    }

    private static Double getSummaryMetricValue(IMusicGrid aggregateGrid, MetricRowKey rowKey, AggregateType columnKey) {
        GridRow row = aggregateGrid.getRow(rowKey);

        if (row != null) {
            GridCell cell = row.getSummaryCell(columnKey.getHeader());
            if (cell != null) {
                return (Double)cell.toValue();
            }
        }
        log.warn("Request for metric row "+rowKey + ", column "+ columnKey + " not found.");
        return null;
    }

    public static IdeaFlowMetrics queryFrom(MusicClock musicClock, IMusicGrid musicGrid) {

        Duration totalDuration = selectTotalDuration(musicClock);
        AverageMetric averageFlame = selectAverageFlame(musicGrid);

        PercentMetric percentWtfMetric = selectPercentWTF(musicGrid);
        PercentMetric percentLearningAndProgressMetric = selectPercentLearningAndProgressNotInWtf(musicClock, musicGrid);
        PercentMetric percentPairingMetric = selectPercentPairing(musicGrid);

        IdeaFlowMetrics metrics = new IdeaFlowMetrics();

        metrics.zoomLevel = musicGrid.getZoomLevel();
        metrics.timeInTile = totalDuration;
        metrics.avgFlame = averageFlame.getAverage();

        metrics.percentWtf = percentWtfMetric.getPercent("wtf");
        metrics.percentLearning = percentLearningAndProgressMetric.getPercent("learning");
        metrics.percentProgress = percentLearningAndProgressMetric.getPercent("progress");
        metrics.percentPairing = percentPairingMetric.getPercent("pairing");

        return metrics;
    }



    private static Duration selectTotalDuration(MusicClock musicClock) {
        return musicClock.getRelativeEnd();
    }

    private static PercentMetric selectPercentWTF(IMusicGrid musicGrid) {
        GridRow row = musicGrid.getRow(FeatureRowKey.FLOW_WTF);

        PercentMetric percentMetric = new PercentMetric();

        for (GridCell cell : row) {
            if (cell.getFeatureType() == IdeaFlowStateType.WTF_STATE) {
                percentMetric.addSample("wtf");
            } else {
                percentMetric.addSample("other");
            }
        }

        percentMetric.calculatePercent("wtf");

        return percentMetric;
    }

    private static PercentMetric selectPercentLearningAndProgressNotInWtf(MusicClock musicClock, IMusicGrid musicGrid) {
        GridRow wtfRow = musicGrid.getRow(FeatureRowKey.FLOW_WTF);
        GridRow learningRow = musicGrid.getRow(FeatureRowKey.FLOW_LEARNING);

        PercentMetric percentMetric = new PercentMetric();

        Iterator<RelativeBeat> beatIterator = musicClock.getForwardsIterator();

        while (beatIterator.hasNext()) {
            RelativeBeat beat = beatIterator.next();

            GridCell wtfCell = wtfRow.getCell(beat);
            GridCell learningCell = learningRow.getCell(beat);

            if (learningCell.getFeatureType() == IdeaFlowStateType.LEARNING_STATE &&
                    !(wtfCell.getFeatureType() == IdeaFlowStateType.WTF_STATE)) {
                percentMetric.addSample("learning");
            } else if (learningCell.getFeatureType() == IdeaFlowStateType.PROGRESS_STATE &&
                    !(wtfCell.getFeatureType() == IdeaFlowStateType.WTF_STATE)) {
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
