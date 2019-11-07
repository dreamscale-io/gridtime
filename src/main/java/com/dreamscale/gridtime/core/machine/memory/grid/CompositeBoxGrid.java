package com.dreamscale.gridtime.core.machine.memory.grid;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.CompoundGridResults;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.MusicClock;
import com.dreamscale.gridtime.core.machine.clock.RelativeBeat;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeatureReference;
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
public class CompositeBoxGrid implements IMusicGrid {

    private final GeometryClock.GridTime gridTime;
    private final MusicClock musicClock;
    private final String gridTitle;

    private Map<FeatureReference, ZoomGrid> boxGrids = DefaultCollections.map();


    public CompositeBoxGrid(String gridTitle, GeometryClock.GridTime gridTime, MusicClock musicClock) {
        this.gridTitle = gridTitle;
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
            ZoomGrid firstGrid = boxGrids.values().iterator().next();
            return firstGrid.getRow(rowKey);
        }

        return null;
    }

    public void addBoxMetric(FeatureReference box, MetricRowKey metricRowKey, RelativeBeat beat, Duration durationWeight, Double metric) {
        ZoomGrid zoomGrid = findOrCreateZoomGrid(box);

        zoomGrid.addWeightedMetric(metricRowKey, beat, durationWeight, metric);
    }

    public Double getMetric(FeatureReference box, MetricRowKey metricRowKey) {

        ZoomGrid zoomGrid = findOrCreateZoomGrid(box);

        GridRow gridRow = zoomGrid.getRow(metricRowKey);
        return (Double) gridRow.getSummaryCell(AggregateType.AVG).toValue();
    }

    public Duration getTotalDuration(FeatureReference box) {

        ZoomGrid zoomGrid = findOrCreateZoomGrid(box);

        return zoomGrid.getTotalDuration();
    }


    private ZoomGrid findOrCreateZoomGrid(FeatureReference boxReference) {
        ZoomGrid grid = boxGrids.get(boxReference);

        if (grid == null) {
            grid = new ZoomGrid("Group:Id:"+boxReference.toDisplayString(), gridTime, musicClock);
            boxGrids.put(boxReference, grid);
        }

        return grid;
    }


    public Duration getTotalDuration() {
        Duration totalDuration = Duration.ZERO;

        for (ZoomGrid grid : boxGrids.values()) {
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

        for (ZoomGrid grid : boxGrids.values()) {
            grid.finish();
        }
    }

    public GridRow getRow(FeatureReference box, Key rowKey) {
        ZoomGrid grid = boxGrids.get(box);

        if (grid != null) {
            return grid.getRow(rowKey);
        }

        return null;
    }

    public List<GridRow> getAllGridRows() {
        List<GridRow> allRows = new ArrayList<>();

        for (ZoomGrid grid : boxGrids.values()) {
            allRows.addAll(grid.getAllGridRows());
        }
        return allRows;
    }

    public CompoundGridResults playAllTracks() {

        CompoundGridResults compoundGrid = new CompoundGridResults(gridTitle);

        for (ZoomGrid grid : boxGrids.values()) {
            compoundGrid.addGrid(grid.playAllTracks());
        }

        return compoundGrid;
    }

    public RelativeBeat getBeat(String gridTimeKey) {
        return musicClock.getBeat(gridTimeKey);
    }


}
