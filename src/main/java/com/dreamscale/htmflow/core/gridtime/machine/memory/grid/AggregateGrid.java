package com.dreamscale.htmflow.core.gridtime.machine.memory.grid;

import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.returns.MusicGridResults;
import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.machine.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.FeatureCache;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference.*;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.GridRow;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.glyph.GlyphReferences;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.analytics.query.FeatureMetrics;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.analytics.query.IdeaFlowMetrics;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.query.aggregate.FeatureTotals;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.query.key.Key;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.query.key.MetricRowKey;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.query.key.TrackSetKey;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.track.PlayableCompositeTrackSet;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.track.WeightedMetricTrack;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.CarryOverContext;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class AggregateGrid implements IMusicGrid {

    private final FeatureCache featureCache;
    private final GeometryClock.GridTime gridTime;
    private final GlyphReferences glyphReferences;
    private final FeatureTotals featureTotals;
    private final MusicClock musicClock;

    private Map<MetricRowKey, WeightedMetricTrack> weightedMetricTracks = DefaultCollections.map();

    private List<GridRow> exportedRows;
    private Map<Key, GridRow> exportedRowsByKey;

    public AggregateGrid(FeatureCache featureCache, GeometryClock.GridTime gridTime, MusicClock musicClock) {
        this.featureCache = featureCache;
        this.gridTime = gridTime;
        this.musicClock = musicClock;

        this.glyphReferences = new GlyphReferences();
        this.featureTotals = new FeatureTotals();
    }

    public RelativeBeat getBeat(String gridTimeKey) {
        return musicClock.getBeat(gridTimeKey);
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
