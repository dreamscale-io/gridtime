package com.dreamscale.gridtime.core.machine.memory.grid.query.metrics;

import com.dreamscale.gridtime.core.domain.tile.zoomable.ZoomableBoxMetricsEntity;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.PlaceReference;
import com.dreamscale.gridtime.core.machine.memory.grid.CompositeBoxGrid;
import com.dreamscale.gridtime.core.machine.memory.grid.IMusicGrid;
import com.dreamscale.gridtime.core.machine.memory.grid.MusicGrid;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.GridMetrics;
import com.dreamscale.gridtime.core.machine.memory.grid.query.aggregate.MetricQuery;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.MetricRowKey;
import com.dreamscale.gridtime.core.machine.memory.type.PlaceType;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Slf4j
public class BoxMetrics implements MetricQuery {

    private PlaceReference box;

    private ZoomLevel zoomLevel;

    private Duration timeInBox;

    private Double avgFlame;

    private Double percentWtf;
    private Double percentLearning;
    private Double percentProgress;
    private Double percentPairing;

    private Double avgFileBatchSize;

    private Double avgTraversalSpeed;
    private Double avgExecutionTime;


//do I put gridTime in here?  Then when I load up these objects, maybe I use a view, and get the grid times.

    public static List<BoxMetrics> queryFromAggregateGrid(CompositeBoxGrid compositeBoxGrid) {

        List<BoxMetrics> boxMetricsList = new ArrayList<>();

        Set<FeatureReference> boxes = compositeBoxGrid.getFeaturesOfType(PlaceType.BOX);

        for (FeatureReference box : boxes) {
            BoxMetrics boxMetrics = new BoxMetrics();

            boxMetrics.setBox((PlaceReference) box);

            boxMetrics.setZoomLevel(compositeBoxGrid.getZoomLevel());

            boxMetrics.setTimeInBox(compositeBoxGrid.getTotalDuration(box));
            boxMetrics.setPercentWtf(compositeBoxGrid.getMetric(box, MetricRowKey.ZOOM_PERCENT_WTF));
            boxMetrics.setPercentLearning(compositeBoxGrid.getMetric(box, MetricRowKey.ZOOM_PERCENT_PROGRESS));
            boxMetrics.setPercentProgress(compositeBoxGrid.getMetric(box, MetricRowKey.ZOOM_PERCENT_WTF));
            boxMetrics.setPercentPairing(compositeBoxGrid.getMetric(box, MetricRowKey.ZOOM_PERCENT_PAIRING));
            boxMetrics.setAvgFlame(compositeBoxGrid.getMetric(box, MetricRowKey.ZOOM_AVG_FLAME));

//            boxMetrics.setAvgExecutionTime(boxGrid.getMetric(box, MetricRowKey.EXECUTION_RUN_TIME));
//            boxMetrics.setAvgTraversalSpeed(boxGrid.getMetric(box, MetricRowKey.FILE_TRAVERSAL_VELOCITY));
//            boxMetrics.setAvgFileBatchSize(boxGrid.getMetric(box, MetricRowKey.FILE_TRAVERSAL_VELOCITY));

            boxMetricsList.add(boxMetrics);

        }

        return boxMetricsList;
    }



    public static List<BoxMetrics> queryFrom(IMusicGrid musicGrid) {

        if (musicGrid instanceof MusicGrid) {
            return queryFromMusicGrid((MusicGrid) musicGrid);
        }

        if (musicGrid instanceof CompositeBoxGrid) {
            return queryFromAggregateGrid((CompositeBoxGrid) musicGrid);
        }

        return Collections.emptyList();
    }

    private static List<BoxMetrics> queryFromMusicGrid(MusicGrid musicGrid) {
        List<BoxMetrics> boxMetricsList = new ArrayList<>();

        Set<FeatureReference> boxes = musicGrid.getFeaturesOfType(PlaceType.BOX);

        for (FeatureReference box : boxes) {
            BoxMetrics boxMetrics = new BoxMetrics();

            boxMetrics.setBox((PlaceReference) box);

            boxMetrics.setZoomLevel(musicGrid.getZoomLevel());

            GridMetrics gridMetrics = musicGrid.getGridMetrics(box);

            boxMetrics.setTimeInBox(gridMetrics.getTotalDuration());
            boxMetrics.setPercentWtf(gridMetrics.getAverageMetric(MetricRowKey.IS_WTF));
            boxMetrics.setPercentLearning(gridMetrics.getAverageMetric(MetricRowKey.IS_LEARNING));
            boxMetrics.setPercentProgress(gridMetrics.getAverageMetric(MetricRowKey.IS_PROGRESS));
            boxMetrics.setPercentPairing(gridMetrics.getAverageMetric(MetricRowKey.IS_PAIRING));

            boxMetrics.setAvgExecutionTime(gridMetrics.getAverageMetric(MetricRowKey.EXECUTION_RUN_TIME));
            boxMetrics.setAvgFlame(gridMetrics.getAverageMetric(MetricRowKey.FEELS));
            boxMetrics.setAvgTraversalSpeed(gridMetrics.getAverageMetric(MetricRowKey.FILE_TRAVERSAL_VELOCITY));

            boxMetrics.setAvgFileBatchSize((double)gridMetrics.getFeatureCount(PlaceType.LOCATION));

            boxMetricsList.add(boxMetrics);

        }

        return boxMetricsList;
    }


    public static BoxMetrics queryFrom(PlaceReference box, ZoomableBoxMetricsEntity boxEntity) {
        BoxMetrics boxMetrics = new BoxMetrics();

        boxMetrics.setBox( box);

        boxMetrics.setZoomLevel(boxEntity.getZoomLevel());

        boxMetrics.setTimeInBox(Duration.ofSeconds(boxEntity.getTimeInBox()));
        boxMetrics.setPercentWtf(boxEntity.getPercentWtf());
        boxMetrics.setPercentLearning(boxEntity.getPercentLearning());
        boxMetrics.setPercentProgress(boxEntity.getPercentProgress());
        boxMetrics.setPercentPairing(boxEntity.getPercentPairing());

        boxMetrics.setAvgExecutionTime(boxEntity.getAvgExecutionTime());
        boxMetrics.setAvgFlame(boxEntity.getAvgFlame());
        boxMetrics.setAvgTraversalSpeed(boxEntity.getAvgTraversalSpeed());

        boxMetrics.setAvgFileBatchSize(boxEntity.getAvgFileBatchSize());

        return boxMetrics;
    }
}
