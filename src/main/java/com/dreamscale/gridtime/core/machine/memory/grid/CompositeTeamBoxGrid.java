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
import com.dreamscale.gridtime.core.machine.memory.grid.track.WeightedMetricTeamTrack;
import com.dreamscale.gridtime.core.machine.memory.type.FeatureType;
import com.dreamscale.gridtime.core.machine.memory.type.PlaceType;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.*;

@Slf4j
public class CompositeTeamBoxGrid implements IMusicGrid {

    private final GeometryClock.GridTime gridTime;
    private final MusicClock musicClock;
    private final String gridTitle;

    private Map<FeatureReference, TeamZoomGrid> teamBoxGrids = DefaultCollections.map();


    public CompositeTeamBoxGrid(String gridTitle, GeometryClock.GridTime gridTime, MusicClock musicClock) {
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
        if (teamBoxGrids.size() > 0) {
            TeamZoomGrid firstGrid = teamBoxGrids.values().iterator().next();
            return firstGrid.getRow(rowKey);
        }

        return null;
    }


    public void addTeamBoxMetric(UUID torchieId, String memberName, FeatureReference box, MetricRowKey metricRowKey, Duration durationWeight, Double metric) {

        TeamZoomGrid teamZoomGrid = findOrCreateTeamZoomGrid(box);

        teamZoomGrid.addColumn(torchieId, memberName);
        teamZoomGrid.addWeightedMetric(torchieId, metricRowKey, durationWeight, metric);
    }

    public void addTimeForColumn(UUID torchieId, FeatureReference box, Duration durationWeight) {

        TeamZoomGrid teamZoomGrid = findOrCreateTeamZoomGrid(box);
        teamZoomGrid.addTimeForColumn(torchieId, durationWeight);
    }


    public Double getMetric(FeatureReference box, MetricRowKey metricRowKey) {

        TeamZoomGrid zoomGrid = findOrCreateTeamZoomGrid(box);

        GridRow gridRow = zoomGrid.getRow(metricRowKey);
        return (Double) gridRow.getSummaryCell(AggregateType.AVG).toValue();
    }

    public Duration getTotalDuration(FeatureReference box) {

        TeamZoomGrid zoomGrid = findOrCreateTeamZoomGrid(box);

        return zoomGrid.getTotalDuration();
    }


    private TeamZoomGrid findOrCreateTeamZoomGrid(FeatureReference boxReference) {
        TeamZoomGrid grid = teamBoxGrids.get(boxReference);

        if (grid == null) {
            grid = new TeamZoomGrid("Box:Id:"+boxReference.toDisplayString(), gridTime);
            teamBoxGrids.put(boxReference, grid);
        }

        return grid;
    }


    public Duration getTotalDuration() {
        Duration totalDuration = Duration.ZERO;

        for (TeamZoomGrid grid : teamBoxGrids.values()) {
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
            return teamBoxGrids.keySet();
        }

        return Collections.emptySet();
    }

    public void finish() {

        for (TeamZoomGrid grid : teamBoxGrids.values()) {
            grid.finish();
        }
    }

    public GridRow getRow(FeatureReference box, Key rowKey) {
        TeamZoomGrid grid = teamBoxGrids.get(box);

        if (grid != null) {
            return grid.getRow(rowKey);
        }

        return null;
    }

    public List<GridRow> getAllGridRows() {
        List<GridRow> allRows = new ArrayList<>();

        for (TeamZoomGrid grid : teamBoxGrids.values()) {
            allRows.addAll(grid.getAllGridRows());
        }
        return allRows;
    }

    public CompoundGridResults playAllTracks() {

        CompoundGridResults compoundGrid = new CompoundGridResults(gridTitle);

        for (TeamZoomGrid grid : teamBoxGrids.values()) {
            compoundGrid.addGrid(grid.playAllTracks());
        }

        return compoundGrid;
    }

    public RelativeBeat getBeat(String gridTimeKey) {
        return musicClock.getBeat(gridTimeKey);
    }


}
