package com.dreamscale.gridtime.core.machine.memory.grid;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.MusicGridResults;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.MusicClock;
import com.dreamscale.gridtime.core.machine.clock.RelativeBeat;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.GridRow;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.GridMetrics;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.Key;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.MetricRowKey;
import com.dreamscale.gridtime.core.machine.memory.type.FeatureType;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.*;

@Slf4j
public class BoxAggregateMetricGrid implements IMusicGrid {

    private final GeometryClock.GridTime gridTime;
    private final MusicClock musicClock;

    private Map<UUID, AggregateMetricGrid> boxGrids = DefaultCollections.map();
    private UUID activeBoxKey;


    public BoxAggregateMetricGrid(GeometryClock.GridTime gridTime, MusicClock musicClock) {
        this.gridTime = gridTime;
        this.musicClock = musicClock;
    }

    @Override
    public ZoomLevel getZoomLevel() {
        return musicClock.getZoomLevel();
    }



    public void addBoxMetric(UUID featureId, MetricRowKey metricRowKey, RelativeBeat beat, Duration durationWeight, Double metric) {
        AggregateMetricGrid aggregateMetricGrid = findOrCreateAggregateGrid(featureId);

        aggregateMetricGrid.addWeightedMetric(metricRowKey, beat, durationWeight, metric);
    }

    private AggregateMetricGrid findOrCreateAggregateGrid(UUID boxFeatureId) {
        AggregateMetricGrid grid = boxGrids.get(boxFeatureId);

        if (grid == null) {
            grid = new AggregateMetricGrid(gridTime, musicClock);
            boxGrids.put(boxFeatureId, grid);
        }

        return grid;
    }


    public Duration getTotalDuration() {
        Duration totalDuration = Duration.ZERO;

        for (AggregateMetricGrid grid : boxGrids.values()) {
            totalDuration = totalDuration.plus(grid.getTotalDuration());
        }

        return totalDuration;
    }

    @Override
    public GridMetrics getGridMetrics(FeatureReference featureReference) {
        return null;
    }

    @Override
    public Set<FeatureReference> getFeaturesOfType(FeatureType featureType) {
        return null;
    }



    public void finish() {

        for (AggregateMetricGrid grid : boxGrids.values()) {
            grid.finish();
        }
    }

    Set<UUID> getBoxKeys() {
        return boxGrids.keySet();
    }


    public void setActiveBoxKey(UUID boxKey) {
        this.activeBoxKey = boxKey;
    }

    public GridRow getRow(Key rowKey) {
        initActiveBoxIfNull();

        if (activeBoxKey != null) {
            AggregateMetricGrid grid = boxGrids.get(activeBoxKey);
            return grid.getRow(rowKey);
        }

        return null;
    }

    private void initActiveBoxIfNull() {
        if (activeBoxKey == null && boxGrids.size() > 0) {
            activeBoxKey = boxGrids.keySet().iterator().next();
        }
    }

    public List<GridRow> getAllGridRows() {
        initActiveBoxIfNull();

        if (activeBoxKey != null) {
            AggregateMetricGrid grid = boxGrids.get(activeBoxKey);
            return grid.getAllGridRows();
        }

        return DefaultCollections.emptyList();
    }

    public MusicGridResults playAllTracks() {
        return toMusicGridResults(getAllGridRows());
    }

    private MusicGridResults toMusicGridResults(List<GridRow> gridRows) {

        List<String> headerRow = new ArrayList<>();
        List<List<String>> valueRows = new ArrayList<>();

        if (gridRows != null && gridRows.size() > 0) {
            GridRow firstRow = gridRows.get(0);

            headerRow.addAll(firstRow.toHeaderColumns());

            for (GridRow gridRow : gridRows) {
                valueRows.add( gridRow.toValueRow());
            }
        }

        return new MusicGridResults(headerRow, valueRows);
    }

    public RelativeBeat getBeat(String gridTimeKey) {
        return musicClock.getBeat(gridTimeKey);
    }

}
