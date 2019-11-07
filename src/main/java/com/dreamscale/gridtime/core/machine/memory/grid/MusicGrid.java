package com.dreamscale.gridtime.core.machine.memory.grid;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.MusicGridResults;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.MusicClock;
import com.dreamscale.gridtime.core.machine.clock.RelativeBeat;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.*;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.GridRow;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.GridMetrics;
import com.dreamscale.gridtime.core.machine.memory.grid.glyph.GlyphReferences;
import com.dreamscale.gridtime.core.machine.memory.grid.landscape.FeatureLandscapeMetrics;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.Key;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.TrackSetKey;
import com.dreamscale.gridtime.core.machine.memory.grid.track.PlayableCompositeTrackSet;
import com.dreamscale.gridtime.core.machine.memory.grid.trackset.*;
import com.dreamscale.gridtime.core.machine.memory.tag.FinishTag;
import com.dreamscale.gridtime.core.machine.memory.tag.StartTag;
import com.dreamscale.gridtime.core.machine.memory.tile.CarryOverContext;
import com.dreamscale.gridtime.core.machine.memory.type.FeatureType;
import com.dreamscale.gridtime.core.machine.memory.type.WorkContextType;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public class MusicGrid implements IMusicGrid {

    private final FeatureCache featureCache;
    private final GeometryClock.GridTime gridTime;
    private final MusicClock musicClock;
    private final GlyphReferences glyphReferences;
    private final FeatureLandscapeMetrics landscapeMetrics;
    private final String title;

    private AuthorsTrackSet authorsTracks;
    private FeelsTrackSet feelsTracks;

    private WorkContextTrackSet contextTracks;
    private IdeaFlowTrackSet ideaflowTracks;

    private ExecutionTrackSet executionTracks;
    private NavigationTrackSet navigationTracks;

    private Map<TrackSetKey, PlayableCompositeTrackSet> trackSetsByKey = DefaultCollections.map();

    private List<GridRow> exportedRows;
    private Map<Key, GridRow> exportedRowsByKey;

    public MusicGrid(String title, FeatureCache featureCache, GeometryClock.GridTime gridTime, MusicClock musicClock) {
        this.title = title;
        this.featureCache = featureCache;
        this.gridTime = gridTime;
        this.musicClock = musicClock;

        this.glyphReferences = new GlyphReferences();
        this.landscapeMetrics = new FeatureLandscapeMetrics();

        this.authorsTracks = new AuthorsTrackSet(TrackSetKey.Authors, gridTime, musicClock);
        this.feelsTracks = new FeelsTrackSet(TrackSetKey.Feels, gridTime, musicClock);

        this.contextTracks = new WorkContextTrackSet(TrackSetKey.WorkContext, gridTime, musicClock);
        this.ideaflowTracks = new IdeaFlowTrackSet(TrackSetKey.IdeaFlow, featureCache, gridTime, musicClock);

        this.executionTracks = new ExecutionTrackSet(TrackSetKey.Executions, gridTime, musicClock);
        this.navigationTracks = new NavigationTrackSet(TrackSetKey.Navigations, featureCache, glyphReferences, gridTime, musicClock);

        addAllTrackSets(authorsTracks, feelsTracks, contextTracks, ideaflowTracks, navigationTracks, executionTracks);

    }

    private void addAllTrackSets(PlayableCompositeTrackSet... trackSets) {
        for (PlayableCompositeTrackSet trackSet : trackSets) {
            trackSetsByKey.put(trackSet.getTrackSetKey(), trackSet);
        }
    }

    public Set<FeatureReference> getAllFeatures() {
        Set<FeatureReference> allFeatures = DefaultCollections.set();

        for (PlayableCompositeTrackSet trackset : trackSetsByKey.values()) {
            allFeatures.addAll(trackset.getFeatures());
        }

        return allFeatures;
    }

    public void startWorkContext(LocalDateTime moment, WorkContextReference workContext) {
        contextTracks.startWorkContext(moment, workContext);
    }

    public void clearWorkContext(LocalDateTime moment, FinishTag finishTag) {
        contextTracks.clearWorkContext(moment, finishTag);
    }

    public void startAuthors(LocalDateTime moment, AuthorsReference authorsReference) {
        authorsTracks.startAuthors(moment, authorsReference);
    }

    public void clearAuthors(LocalDateTime moment) {
        authorsTracks.clearAuthors(moment);
    }


    public void startFeels(LocalDateTime moment, FeelsReference feelsState) {
        feelsTracks.startFeels(moment, feelsState);
    }

    public void clearFeels(LocalDateTime moment) {
        feelsTracks.clearFeels(moment);
    }

    public WorkContextReference getLastContextOnOrBeforeMoment(LocalDateTime moment, WorkContextType fromStructureLevel) {
        return contextTracks.getLastOnOrBeforeMoment(moment, fromStructureLevel);
    }

    public void startWTF(LocalDateTime moment, IdeaFlowStateReference wtfState, StartTag startTag) {
       ideaflowTracks.startWTF(moment, wtfState, startTag);
    }

    public void clearWTF(LocalDateTime moment, FinishTag finishTag) {
        ideaflowTracks.clearWTF(moment, finishTag);
    }

    public void gotoLocation(LocalDateTime moment, PlaceReference nextLocation, Duration timeInLocation) {
        PlaceReference lastLocation = navigationTracks.getLastLocation();

        if (lastLocation != null) {
            PlaceReference traversalReference = featureCache.lookupTraversalReference(lastLocation, nextLocation);
            landscapeMetrics.getMetricsFor(traversalReference).addVelocitySample(timeInLocation);
        }

        landscapeMetrics.getMetricsFor(nextLocation).addVelocitySample(timeInLocation);

        PlaceReference boxReference = featureCache.lookupBoxReference(nextLocation);
        landscapeMetrics.getMetricsFor(boxReference).addVelocitySample(timeInLocation);
        landscapeMetrics.getMetricsFor(boxReference).countFeature(nextLocation);

        navigationTracks.gotoLocation(moment, nextLocation, timeInLocation);
    }

    public void modifyCurrentLocation(LocalDateTime moment, int modificationCount) {
        RelativeBeat beat = musicClock.getClosestBeat(gridTime.getRelativeTime(moment));

        PlaceReference lastLocation = navigationTracks.getLastLocationBeforeMoment(moment);
        if (lastLocation != null) {
            PlaceReference boxReference = featureCache.lookupBoxReference(lastLocation);
            landscapeMetrics.getMetricsFor(boxReference).addModificationSample(modificationCount);
            landscapeMetrics.getMetricsFor(lastLocation).addModificationSample(modificationCount);
        }

        WorkContextReference lastProject = contextTracks.getLastOnOrBeforeMoment(moment, WorkContextType.PROJECT_WORK);
        WorkContextReference lastTask = contextTracks.getLastOnOrBeforeMoment(moment, WorkContextType.TASK_WORK);
        WorkContextReference lastIntention = contextTracks.getLastOnOrBeforeMoment(moment, WorkContextType.INTENTION_WORK);

        landscapeMetrics.getMetricsFor(lastProject).addModificationSample(modificationCount);
        landscapeMetrics.getMetricsFor(lastTask).addModificationSample(modificationCount);
        landscapeMetrics.getMetricsFor(lastIntention).addModificationSample(modificationCount);

        ideaflowTracks.addModificationSampleForLearningBand(beat, modificationCount);
    }

    public void executeThing(ExecutionReference execution) {
        LocalDateTime moment = execution.getPosition();

        PlaceReference lastLocation = navigationTracks.getLastLocationBeforeMoment(moment);
        if (lastLocation != null) {
            PlaceReference boxReference = featureCache.lookupBoxReference(lastLocation);
            landscapeMetrics.getMetricsFor(boxReference).addExecutionTimeSample(execution.getExecutionTime());
        }

        WorkContextReference lastProject = contextTracks.getLastOnOrBeforeMoment(moment, WorkContextType.PROJECT_WORK);
        WorkContextReference lastTask = contextTracks.getLastOnOrBeforeMoment(moment, WorkContextType.TASK_WORK);
        WorkContextReference lastIntention = contextTracks.getLastOnOrBeforeMoment(moment, WorkContextType.INTENTION_WORK);

        landscapeMetrics.getMetricsFor(lastProject).addExecutionTimeSample(execution.getExecutionTime());
        landscapeMetrics.getMetricsFor(lastTask).addExecutionTimeSample(execution.getExecutionTime());
        landscapeMetrics.getMetricsFor(lastIntention).addExecutionTimeSample(execution.getExecutionTime());

        executionTracks.executeThing(moment, execution);
    }

    public void finish() {

        for (PlayableCompositeTrackSet trackSet : trackSetsByKey.values()) {
            trackSet.finish();
        }

        finishBoxMetrics();

        exportGridRows();
    }

    private void finishBoxMetrics() {
        Iterator<RelativeBeat> iterator = musicClock.getForwardsIterator();

        while (iterator.hasNext()) {

            RelativeBeat beat = iterator.next();
            PlaceReference boxReference = navigationTracks.getBoxAtBeat(beat);

            if (boxReference != null) {
                GridMetrics boxMetrics = landscapeMetrics.getMetricsFor(boxReference);

                ideaflowTracks.populateBoxWithBeat(beat, boxMetrics);
                feelsTracks.populateBoxWithBeat(beat, boxMetrics);
                authorsTracks.populateBoxWithBeat(beat, boxMetrics);
                navigationTracks.populateBoxWithBeat(beat, boxMetrics);
                executionTracks.populateBoxWithBeat(beat, boxMetrics);

                //push into red vs green time on completion, whenever completion event, save the metric
                //with the box
            }

            //calculate and save the landscape

            //only include relative to positive activity within the box...
            // such that, the total of time spent within the box, within the 20 minutes, becomes the overall weight of all box metrics

//            private Integer timeInPairing;
//
//            private Float avgBatchSize;
//
//            private Float avgTraversalSpeed;
//
//            private Float avgExecutionTime;
//
//            private Float avgRedToGreenTime;
//
//
//            int batchSize = navigationTracks.getBatchSizeAtBeat(beat); //turn this into 5 min slider similar to modify track
//
//            double traversalSpeed = 1; //turn this into continuously slowing down when there's no traversal, speed is this +1, so there's a sort of decay


        }
    }



    public MusicGridResults playTrack(TrackSetKey trackToPlay) {
        PlayableCompositeTrackSet trackSet = trackSetsByKey.get(trackToPlay);

        return toMusicGridResults(trackSet.toGridRows());
    }

    @Override
    public ZoomLevel getZoomLevel() {
        return musicClock.getZoomLevel();
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

    @Override
    public Duration getTotalDuration() {
        //TODO this will need to take idle time into account, and calculate the positive time in the frame
        return musicClock.getRelativeEnd();
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

        return new MusicGridResults(title, headerRow, valueRows);
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


    public List<PlaceReference> getBoxesVisted() {
        return landscapeMetrics.getBoxesVisited();
    }

    public Iterator<RelativeBeat> iterateBeats() {
        return musicClock.getForwardsIterator();
    }

    public GridMetrics getBoxMetricsAtBeat(RelativeBeat beat) {
        PlaceReference boxReference = navigationTracks.getBoxAtBeat(beat);
        return landscapeMetrics.getMetricsFor(boxReference);
    }

    public GridMetrics getGridMetrics(FeatureReference feature) {
        return landscapeMetrics.getMetricsFor(feature);
    }

    @Override
    public Set<FeatureReference> getFeaturesOfType(FeatureType featureType) {
        return landscapeMetrics.getFeaturesOfType(featureType);
    }
}
