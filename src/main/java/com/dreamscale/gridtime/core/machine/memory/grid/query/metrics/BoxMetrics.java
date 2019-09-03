package com.dreamscale.gridtime.core.machine.memory.grid.query.metrics;

import com.dreamscale.gridtime.core.machine.clock.MusicClock;
import com.dreamscale.gridtime.core.machine.clock.RelativeBeat;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.AuthorsReference;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeelsReference;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.PlaceReference;
import com.dreamscale.gridtime.core.machine.memory.grid.IMusicGrid;
import com.dreamscale.gridtime.core.machine.memory.grid.MusicGrid;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.GridRow;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.AggregateType;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.GridMetrics;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.type.GridCell;
import com.dreamscale.gridtime.core.machine.memory.grid.query.aggregate.AverageMetric;
import com.dreamscale.gridtime.core.machine.memory.grid.query.aggregate.MetricQuery;
import com.dreamscale.gridtime.core.machine.memory.grid.query.aggregate.PercentMetric;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.FeatureRowKey;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.MetricRowKey;
import com.dreamscale.gridtime.core.machine.memory.type.AuthorsType;
import com.dreamscale.gridtime.core.machine.memory.type.IdeaFlowStateType;
import com.dreamscale.gridtime.core.machine.memory.type.PlaceType;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    public static List<BoxMetrics> queryFrom(MusicGrid musicGrid) {

        List<BoxMetrics> boxMetricsList = new ArrayList<>();

        List<PlaceReference> boxes = musicGrid.getBoxesVisted();

        for (PlaceReference box : boxes) {
            BoxMetrics boxMetrics = new BoxMetrics();

            boxMetrics.setBox(box);

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


}
