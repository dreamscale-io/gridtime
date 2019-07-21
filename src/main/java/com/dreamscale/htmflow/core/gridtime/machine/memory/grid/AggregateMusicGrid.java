package com.dreamscale.htmflow.core.gridtime.machine.memory.grid;

import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.returns.MusicGridResults;
import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.FeatureCache;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference.*;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.GridRow;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.glyph.GlyphReferences;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.analytics.query.FeatureMetrics;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.analytics.query.IdeaFlowMetrics;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.query.aggregate.FeatureTotals;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.query.key.Key;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.query.key.TrackSetKey;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.track.PlayableCompositeTrackSet;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.CarryOverContext;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class AggregateMusicGrid implements IMusicGrid {

    private final FeatureCache featureCache;
    private final GeometryClock.GridTime gridTime;
    private final MusicClock musicClock;
    private final GlyphReferences glyphReferences;
    private final FeatureTotals featureTotals;


    private Map<TrackSetKey, PlayableCompositeTrackSet> trackSetsByKey = DefaultCollections.map();

    private List<GridRow> exportedRows;
    private Map<Key, GridRow> exportedRowsByKey;

    public AggregateMusicGrid(FeatureCache featureCache, GeometryClock.GridTime gridTime, MusicClock musicClock) {
        this.featureCache = featureCache;
        this.gridTime = gridTime;
        this.musicClock = musicClock;

        this.glyphReferences = new GlyphReferences();
        this.featureTotals = new FeatureTotals();
    }

    public void loadTileMetrics(LocalDateTime moment, IdeaFlowMetrics metrics) {

    }

    public void finish() {

        for (PlayableCompositeTrackSet trackSet : trackSetsByKey.values()) {
            trackSet.finish();
        }

        exportGridRows();
    }

    public MusicGridResults playTrack(TrackSetKey trackToPlay) {
        PlayableCompositeTrackSet trackSet = trackSetsByKey.get(trackToPlay);

        return toMusicGridResults(trackSet.toGridRows());
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

            for (TrackSetKey trackSetKey : trackSetsByKey.keySet()) {
                PlayableCompositeTrackSet track = trackSetsByKey.get(trackSetKey);

                exportedRows.addAll(track.toGridRows());
            }

            exportedRowsByKey = DefaultCollections.map();

            for (GridRow row: exportedRows) {
                exportedRowsByKey.put(row.getRowKey(), row);
            }
        }
    }


    public CarryOverContext getCarryOverContext() {
        log.info("getCarryOverContext");
        CarryOverContext carryOverContext = new CarryOverContext("[MusicGrid]");

        for (PlayableCompositeTrackSet trackSet : trackSetsByKey.values()) {
            String subcontextName = getSubcontextName(trackSet);
            carryOverContext.addSubContext(trackSet.getCarryOverContext(subcontextName));
        }

        return carryOverContext;
    }


    public void initFromCarryOverContext(CarryOverContext carryOverContext) {
        log.info("initFromCarryOverContext");

        for (PlayableCompositeTrackSet trackSet : trackSetsByKey.values()) {
            String subcontextName = getSubcontextName(trackSet);
            trackSet.initFromCarryOverContext(carryOverContext.getSubContext(subcontextName));
        }

    }

    private String getSubcontextName(PlayableCompositeTrackSet compositeTrack) {
        return "[MusicGrid." + compositeTrack.getTrackSetKey().name() + "]";
    }


    public void loadFeatureMetrics(FeatureReference featureReference, FeatureMetrics featureMetrics) {

    }
}
