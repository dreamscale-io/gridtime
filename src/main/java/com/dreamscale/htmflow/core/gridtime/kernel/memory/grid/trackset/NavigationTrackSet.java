package com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.trackset;

import com.dreamscale.htmflow.core.gridtime.kernel.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.kernel.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.cache.FeatureCache;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.PlaceReference;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.metrics.AggregateType;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.GridRow;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.query.key.FeatureRowKey;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.query.key.MetricRowKey;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.glyph.GlyphReferences;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.query.key.TrackSetKey;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track.*;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tile.CarryOverContext;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public class NavigationTrackSet implements PlayableCompositeTrackSet {

    private final TrackSetKey trackSetName;
    private final FeatureCache featureCache;
    private final GeometryClock.GridTime gridTime;
    private final MusicClock musicClock;

    private final RhythmMusicTrack<PlaceReference> rhythmTrack;
    private final BatchMusicTrack<PlaceReference> batchTrack;
    private final BatchMusicTrack<PlaceReference> boxTrack;

    private final MetricsTrack speedTrack;
    private final GlyphReferences glyphReferences;



    private PlaceReference carryOverLastLocation;

    private PlaceReference lastLocation;

    public NavigationTrackSet(TrackSetKey trackSetName, FeatureCache featureCache, GlyphReferences glyphReferences, GeometryClock.GridTime gridTime, MusicClock musicClock) {
        this.trackSetName = trackSetName;
        this.featureCache = featureCache;
        this.glyphReferences = glyphReferences;
        this.gridTime = gridTime;
        this.musicClock = musicClock;

        this.boxTrack = new BatchMusicTrack<>(FeatureRowKey.NAV_BOX, gridTime, musicClock);
        this.batchTrack = new BatchMusicTrack<>(FeatureRowKey.NAV_BATCH, gridTime, musicClock);
        this.rhythmTrack = new RhythmMusicTrack<>(FeatureRowKey.NAV_RHYTHM, gridTime, musicClock);

        this.speedTrack = new MetricsTrack(gridTime, musicClock);

    }

    public void gotoLocation(LocalDateTime moment, PlaceReference locationReference, Duration durationInPlace) {

        PlaceReference boxReference = featureCache.lookupBoxReference(locationReference);

        glyphReferences.addBoxGlyph(boxReference);
        boxTrack.playEventAtBeat(moment, boxReference);

        glyphReferences.addLocationGlyph(locationReference);
        rhythmTrack.playEventAtBeat(moment, locationReference);
        batchTrack.playEventAtBeat(moment, locationReference);

        lastLocation = locationReference;

        RelativeBeat beat = musicClock.getClosestBeat(gridTime.getRelativeTime(moment));
        speedTrack.getMetricsFor(beat).addVelocitySample(durationInPlace);
    }

    public PlaceReference getLastLocationBeforeMoment(LocalDateTime moment) {
        PlaceReference location = rhythmTrack.findNearestEventBeforeMoment(moment);
        if (location == null) {
            location = getLastLocation();
        }
        return location;
    }

    public PlaceReference getLastLocation() {
        if (lastLocation == null) {
            return carryOverLastLocation;
        } else {
            return lastLocation;
        }
    }

    public void finish() {

        Iterator<RelativeBeat> beatIterator = musicClock.getForwardsIterator();

        PlaceReference lastBox = null;
        PlaceReference lastLocation = null;

        while(beatIterator.hasNext()) {
            RelativeBeat beat = beatIterator.next();
            List<PlaceReference> locations = rhythmTrack.getAllFeaturesAtBeat(beat);

            if (locations.size() > 0) {
                lastLocation = locations.get(locations.size() - 1);
                lastBox = featureCache.lookupBoxReference(lastLocation);
            } else {
                if (lastBox != null) {
                    boxTrack.playEventAtBeat(beat, lastBox);
                }
                if (lastLocation != null) {
                    batchTrack.playEventAtBeat(beat, lastLocation);
                }
            }
        }
    }



    public CarryOverContext getCarryOverContext(String subcontextName) {

        CarryOverContext carryOverContext = new CarryOverContext(subcontextName);

        PlaceReference lastLocation = rhythmTrack.getLast();
        carryOverContext.saveReference("last.location", lastLocation);

        return carryOverContext;
    }

    public void initFromCarryOverContext(CarryOverContext subContext) {
        carryOverLastLocation = subContext.getReference("last.location");
    }

    public TrackSetKey getTrackSetKey() {
        return trackSetName;
    }

    @Override
    public List<GridRow> toGridRows() {
        List<GridRow> rows = new ArrayList<>();

        rows.add(boxTrack.toGridRow(glyphReferences.getBoxGlyphMappings()));
        rows.add(batchTrack.toGridRow(glyphReferences.getLocationGlyphMappings()));
        rows.add(rhythmTrack.toGridRow(glyphReferences.getLocationGlyphMappings()));

        rows.add(speedTrack.toGridRow(MetricRowKey.FILE_TRAVERSAL_VELOCITY, AggregateType.AVG));

        return rows;
    }

    public Set<FeatureReference> getFeatures() {
        Set<FeatureReference> locationFeatures = batchTrack.getFeatures();
        Set<FeatureReference> boxFeatures = boxTrack.getFeatures();

        Set<FeatureReference> combinedSet = DefaultCollections.set();
        combinedSet.addAll(locationFeatures);
        combinedSet.addAll(boxFeatures);

        return combinedSet;
    }



}
