package com.dreamscale.gridtime.core.machine.memory.grid.trackset;

import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.MusicClock;
import com.dreamscale.gridtime.core.machine.clock.RelativeBeat;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.PlaceReference;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.AggregateType;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.GridRow;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.GridMetrics;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.FeatureRowKey;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.MetricRowKey;
import com.dreamscale.gridtime.core.machine.memory.grid.glyph.GlyphReferences;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.TrackSetKey;
import com.dreamscale.gridtime.core.machine.memory.grid.track.*;
import com.dreamscale.gridtime.core.machine.memory.tile.CarryOverContext;
import com.dreamscale.gridtime.core.machine.memory.type.PlaceType;
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


    private final BatchMusicTrack<PlaceReference> boxTrack;
    private final RhythmMusicTrack<PlaceReference> bridgeTrack;

    private final RhythmMusicTrack<PlaceReference> rhythmTrack;
    private final BatchMusicTrack<PlaceReference> batchTrack;

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
        this.bridgeTrack = new RhythmMusicTrack<>(FeatureRowKey.NAV_BRIDGE, gridTime, musicClock);

        this.rhythmTrack = new RhythmMusicTrack<>(FeatureRowKey.NAV_RHYTHM, gridTime, musicClock);
        this.batchTrack = new BatchMusicTrack<>(FeatureRowKey.NAV_BATCH, gridTime, musicClock);

        this.speedTrack = new MetricsTrack(gridTime, musicClock);
    }

    public void gotoLocation(LocalDateTime moment, PlaceReference nextLocation, Duration durationInPlace) {

        PlaceReference boxReference = featureCache.lookupBoxReference(nextLocation);

        glyphReferences.addBoxGlyph(boxReference);
        boxTrack.playEventAtBeat(moment, boxReference);

        glyphReferences.addLocationGlyph(nextLocation);
        rhythmTrack.playEventAtBeat(moment, nextLocation);
        batchTrack.playEventAtBeat(moment, nextLocation);

        if (getLastLocation() != null) {
            PlaceReference traversal = featureCache.lookupTraversalReference(getLastLocation(), nextLocation);
            if (traversal.getPlaceType() == PlaceType.BRIDGE_BETWEEN_BOXES) {
                glyphReferences.addTraversalGlyph(traversal);
                bridgeTrack.playEventAtBeat(moment, traversal);
            }
        }

        lastLocation = nextLocation;

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

    public PlaceReference getBoxAtBeat(RelativeBeat beat) {
        return boxTrack.getLastFeatureAt(beat);
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

    @Override
    public void populateBoxWithBeat(RelativeBeat beat, GridMetrics boxMetrics) {

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
        rows.add(bridgeTrack.toGridRow(glyphReferences.getTraversalGlyphMappings()));

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
