package com.dreamscale.gridtime.core.machine.memory.grid;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.MusicGridResults;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.MusicClock;
import com.dreamscale.gridtime.core.machine.clock.RelativeBeat;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.PlaceReference;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.GridRow;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.AggregateType;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.GridMetrics;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.Key;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.MetricRowKey;
import com.dreamscale.gridtime.core.machine.memory.type.FeatureType;
import com.dreamscale.gridtime.core.machine.memory.type.PlaceType;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.*;

@Slf4j
public class BoxAggregateMetricGrid implements IMusicGrid {

    private final GeometryClock.GridTime gridTime;
    private final MusicClock musicClock;

    private Map<FeatureReference, AggregateMetricGrid> boxGrids = DefaultCollections.map();


    public BoxAggregateMetricGrid(GeometryClock.GridTime gridTime, MusicClock musicClock) {
        this.gridTime = gridTime;
        this.musicClock = musicClock;
    }

    @Override
    public ZoomLevel getZoomLevel() {
        return musicClock.getZoomLevel();
    }

    @Override
    public GridRow getRow(Key rowKey) {
        if (boxGrids.size() > 0) {
            AggregateMetricGrid firstGrid = boxGrids.values().iterator().next();
            return firstGrid.getRow(rowKey);
        }

        return null;
    }

    public void addBoxMetric(FeatureReference box, MetricRowKey metricRowKey, RelativeBeat beat, Duration durationWeight, Double metric) {
        AggregateMetricGrid aggregateMetricGrid = findOrCreateAggregateGrid(box);

        aggregateMetricGrid.addWeightedMetric(metricRowKey, beat, durationWeight, metric);
    }

    public Double getMetric(FeatureReference box, MetricRowKey metricRowKey) {

        AggregateMetricGrid aggregateMetricGrid = findOrCreateAggregateGrid(box);

        GridRow gridRow = aggregateMetricGrid.getRow(metricRowKey);
        return (Double) gridRow.getSummaryCell(AggregateType.AVG).toValue();
    }

    public Duration getTotalDuration(FeatureReference box) {

        AggregateMetricGrid aggregateMetricGrid = findOrCreateAggregateGrid(box);

        return aggregateMetricGrid.getTotalDuration();
    }


    private AggregateMetricGrid findOrCreateAggregateGrid(FeatureReference boxReference) {
        AggregateMetricGrid grid = boxGrids.get(boxReference);

        if (grid == null) {
            grid = new AggregateMetricGrid(gridTime, musicClock);
            boxGrids.put(boxReference, grid);
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
        if (featureType == PlaceType.BOX) {
            return boxGrids.keySet();
        }

        return Collections.emptySet();
    }


    public void finish() {

        for (AggregateMetricGrid grid : boxGrids.values()) {
            grid.finish();
        }
    }

    public GridRow getRow(FeatureReference box, Key rowKey) {
        AggregateMetricGrid grid = boxGrids.get(box);

        if (grid != null) {
            return grid.getRow(rowKey);
        }

        return null;
    }


    public List<GridRow> getAllGridRows() {

        List<GridRow> allRows = new ArrayList<>();

        for (FeatureReference box: boxGrids.keySet()) {
            allRows.addAll(boxGrids.get(box).getAllGridRows());
        }

        return allRows;
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
