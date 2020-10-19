package com.dreamscale.gridtime.core.machine.memory.grid;

import com.dreamscale.gridtime.api.grid.GridTableResults;
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
import com.dreamscale.gridtime.core.machine.memory.grid.track.WeightedMetricTrack;
import com.dreamscale.gridtime.core.machine.memory.type.FeatureType;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.*;

@Slf4j
public class ZoomGrid implements IMusicGrid {

    private final GeometryClock.GridTime gridTime;
    private final MusicClock musicClock;
    private final String groupTitle;

    private Map<MetricRowKey, WeightedMetricTrack> weightedMetricTracks = DefaultCollections.map();

    private List<GridRow> exportedRows;
    private Map<Key, GridRow> exportedRowsByKey;

    public ZoomGrid(String groupTitle, GeometryClock.GridTime gridTime, MusicClock musicClock) {
        this.groupTitle = groupTitle;
        this.gridTime = gridTime;
        this.musicClock = musicClock;
    }

    public RelativeBeat getBeat(String gridTimeKey) {
        return musicClock.getBeat(gridTimeKey);
    }

    public ZoomLevel getZoomLevel() {
        return musicClock.getZoomLevel();
    }

    public void addDurationAtBeat(RelativeBeat beat, Duration duration) {
        WeightedMetricTrack timeTrack = findOrCreateWeightedMetricTrack(MetricRowKey.ZOOM_DURATION_IN_TILE);
        timeTrack.addWeightedMetric(beat, Duration.ofSeconds(1), (double)duration.getSeconds());
    }

    public Duration getTotalDuration() {
        WeightedMetricTrack timeTrack = findOrCreateWeightedMetricTrack(MetricRowKey.ZOOM_DURATION_IN_TILE);
        if (timeTrack != null) {
            return Duration.ofSeconds(timeTrack.getTotalCalculation().longValue());
        } else {
            return Duration.ZERO;
        }
    }

    @Override
    public GridMetrics getGridMetrics(FeatureReference featureReference) {
        return null;
    }

    @Override
    public Set<FeatureReference> getFeaturesOfType(FeatureType featureType) {
        return null;
    }

    public void addWeightedMetric(MetricRowKey metricRowKey, RelativeBeat beat, Duration durationWeight, Double metric) {
        WeightedMetricTrack track = findOrCreateWeightedMetricTrack(metricRowKey);

        track.addWeightedMetric(beat, durationWeight, metric);
    }


    private WeightedMetricTrack findOrCreateWeightedMetricTrack(MetricRowKey metricRowKey) {
        WeightedMetricTrack track = weightedMetricTracks.get(metricRowKey);

        if (track == null) {
            track = new WeightedMetricTrack(metricRowKey, musicClock);
            weightedMetricTracks.put(metricRowKey, track);
        }

        return track;
    }


    public void finish() {

        for (WeightedMetricTrack track : weightedMetricTracks.values()) {
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

    public GridTableResults playAllTracks() {
        return toMusicGridResults(exportedRows);
    }

    private GridTableResults toMusicGridResults(List<GridRow> gridRows) {

        List<String> headerRow = new ArrayList<>();
        List<List<String>> valueRows = new ArrayList<>();

        if (gridRows != null && gridRows.size() > 0) {
            GridRow firstRow = gridRows.get(0);

            headerRow.addAll(firstRow.toHeaderColumns());

            for (GridRow gridRow : gridRows) {
                valueRows.add( gridRow.toValueRow());
            }
        }

        return new GridTableResults(groupTitle, headerRow, valueRows);
    }

    private void exportGridRows() {

        if (exportedRows == null) {
            exportedRows = DefaultCollections.list();

            for (MetricRowKey metricRowKey : weightedMetricTracks.keySet()) {
                WeightedMetricTrack track = weightedMetricTracks.get(metricRowKey);

                exportedRows.addAll(track.toGridRows());
            }

            exportedRowsByKey = DefaultCollections.map();

            for (GridRow row: exportedRows) {
                exportedRowsByKey.put(row.getRowKey(), row);
            }
        }
    }



}
