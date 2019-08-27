package com.dreamscale.gridtime.core.machine.memory.grid;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.MusicGridResults;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.GridRow;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.Key;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.MetricRowKey;
import com.dreamscale.gridtime.core.machine.memory.grid.track.WeightedMetricTeamTrack;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class TeamGrid implements IMusicGrid {

    private final GeometryClock.GridTime gridTime;

    private Map<MetricRowKey, WeightedMetricTeamTrack> weightedMetricTracks = DefaultCollections.map();

    private List<GridRow> exportedRows;
    private Map<Key, GridRow> exportedRowsByKey;

    private Map<UUID, String> columnHeaderMap = DefaultCollections.map();

    public TeamGrid(GeometryClock.GridTime gridTime) {
        this.gridTime = gridTime;

    }

    public void addColumn(UUID torchieId, String columnHeader) {
        columnHeaderMap.put(torchieId, columnHeader);
    }

    public void addTimeForColumn(UUID torchieId, Duration duration) {

        String columnHeader = columnHeaderMap.get(torchieId);

        WeightedMetricTeamTrack timeTrack = findOrCreateWeightedMetricTrack(MetricRowKey.ZOOM_DURATION_IN_TILE);
        timeTrack.addWeightedMetric(torchieId, columnHeader, Duration.ofSeconds(1), (double)duration.getSeconds());

    }

    public void addWeightedMetric(UUID torchieId, MetricRowKey metricRowKey, Duration durationWeight, Double metric) {
        String columnHeader = columnHeaderMap.get(torchieId);

        WeightedMetricTeamTrack track = findOrCreateWeightedMetricTrack(metricRowKey);

        track.addWeightedMetric(torchieId, columnHeader, durationWeight, metric);
    }

    @Override
    public ZoomLevel getZoomLevel() {
        return gridTime.getZoomLevel();
    }

    public Duration getTotalDuration() {
        WeightedMetricTeamTrack timeTrack = findOrCreateWeightedMetricTrack(MetricRowKey.ZOOM_DURATION_IN_TILE);
        if (timeTrack != null) {
            return Duration.ofSeconds(timeTrack.getTotalCalculation().longValue());
        } else {
            return Duration.ZERO;
        }
    }


    private WeightedMetricTeamTrack findOrCreateWeightedMetricTrack(MetricRowKey metricRowKey) {
        WeightedMetricTeamTrack track = weightedMetricTracks.get(metricRowKey);

        if (track == null) {
            track = new WeightedMetricTeamTrack(metricRowKey);
            weightedMetricTracks.put(metricRowKey, track);
        }

        return track;
    }


    public void finish() {

        for (WeightedMetricTeamTrack track : weightedMetricTracks.values()) {
            track.finish();
        }

        exportGridRows();
    }



    public GridRow getRow(Key rowKey) {
        return exportedRowsByKey.get(rowKey);
    }

    public List<GridRow> getAllGridRows() {
        return exportedRows;
    }

    public MusicGridResults playAllTracks() {
        return toMusicGridResults(exportedRows);
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

    private void exportGridRows() {

        if (exportedRows == null) {
            exportedRows = DefaultCollections.list();

            for (MetricRowKey metricRowKey : weightedMetricTracks.keySet()) {
                WeightedMetricTeamTrack track = weightedMetricTracks.get(metricRowKey);

                exportedRows.addAll(track.toGridRows());
            }

            exportedRowsByKey = DefaultCollections.map();

            for (GridRow row: exportedRows) {
                exportedRowsByKey.put(row.getRowKey(), row);
            }
        }
    }



}
