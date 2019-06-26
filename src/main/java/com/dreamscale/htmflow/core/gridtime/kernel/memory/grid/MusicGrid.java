package com.dreamscale.htmflow.core.gridtime.kernel.memory.grid;

import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.returns.MusicGridResults;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.kernel.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.cache.FeatureCache;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.*;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.GridRow;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track.PlayableCompositeTrack;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track.TrackSetName;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.trackset.*;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tag.FinishTag;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tag.StartTag;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tile.CarryOverContext;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tile.IdeaFlowTile;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.type.WorkContextType;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class MusicGrid {

    private final GeometryClock.GridTime gridTime;
    private final MusicClock musicClock;

    private AuthorsTrackSet authorsTracks;
    private FeelsTrackSet feelsTracks;

    private WorkContextTrackSet contextTracks;
    private IdeaFlowTrackSet ideaflowTracks;

    private ExecutionTrackSet executionTracks;
    private NavigationTrackSet navigationTracks;

    private Map<TrackSetName, PlayableCompositeTrack> trackSetsByName = DefaultCollections.map();

    private List<GridRow> allExtractedGridRows;

    public MusicGrid(GeometryClock.GridTime gridTime, MusicClock musicClock) {
        this.gridTime = gridTime;
        this.musicClock = musicClock;

        this.authorsTracks = new AuthorsTrackSet(TrackSetName.Authors, gridTime, musicClock);
        this.feelsTracks = new FeelsTrackSet(TrackSetName.Feels, gridTime, musicClock);

        this.contextTracks = new WorkContextTrackSet(TrackSetName.WorkContext, gridTime, musicClock);
        this.ideaflowTracks = new IdeaFlowTrackSet(TrackSetName.IdeaFlow, gridTime, musicClock);

        this.executionTracks = new ExecutionTrackSet(TrackSetName.Executions, gridTime, musicClock);
        this.navigationTracks = new NavigationTrackSet(TrackSetName.Navigations, gridTime, musicClock);

        addAllTrackSets(authorsTracks, feelsTracks, contextTracks, ideaflowTracks, navigationTracks, executionTracks);

    }

    private void addAllTrackSets(PlayableCompositeTrack ... trackSets) {
        for (PlayableCompositeTrack trackSet : trackSets) {
            trackSetsByName.put(trackSet.getTrackSetName(), trackSet);
        }
    }

    public Set<FeatureReference> getAllFeatures() {
        Set<FeatureReference> allFeatures = DefaultCollections.set();

        for (PlayableCompositeTrack trackset : trackSetsByName.values()) {
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

    public void gotoLocation(LocalDateTime moment, PlaceReference locationInBox, Duration timeInLocation) {
        navigationTracks.gotoPlace(moment, locationInBox, timeInLocation);
    }

    public void modifyCurrentLocation(LocalDateTime moment, int modificationCount) {
        RelativeBeat beat = musicClock.getClosestBeat(gridTime.getRelativeTime(moment));

        navigationTracks.modifyPlace(beat, modificationCount);
        ideaflowTracks.addModificationSampleForLearningBand(beat, modificationCount);
    }

    public void executeThing(ExecutionReference execution) {
        executionTracks.executeThing(execution.getPosition(), execution);
    }

    public void finish(FeatureCache featureCache) {
        authorsTracks.finish();
        feelsTracks.finish();
        contextTracks.finish();
        ideaflowTracks.finish(featureCache);
    }

    public IdeaFlowTile getIdeaFlowTile() {
        return null;
    }

    public MusicGridResults playTrackSet(TrackSetName trackToPlay) {
        PlayableCompositeTrack trackSet = trackSetsByName.get(trackToPlay);

        return toMusicGridResults(trackSet.toGridRows());
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

    public List<GridRow> getAllGridRows() {

        if (allExtractedGridRows == null) {

            allExtractedGridRows = new ArrayList<>();

            for (TrackSetName trackName : trackSetsByName.keySet()) {
                PlayableCompositeTrack track = trackSetsByName.get(trackName);

                allExtractedGridRows.addAll(track.toGridRows());
            }
        }

        return allExtractedGridRows;
    }


    public CarryOverContext getCarryOverContext() {
        log.info("getCarryOverContext");
        CarryOverContext carryOverContext = new CarryOverContext("[MusicGrid]");

        for (PlayableCompositeTrack trackSet : trackSetsByName.values()) {
            String subcontextName = getSubcontextName(trackSet);
            carryOverContext.addSubContext(trackSet.getCarryOverContext(subcontextName));
        }

        return carryOverContext;
    }


    public void initFromCarryOverContext(CarryOverContext carryOverContext) {
        log.info("initFromCarryOverContext");

        for (PlayableCompositeTrack trackSet : trackSetsByName.values()) {
            String subcontextName = getSubcontextName(trackSet);
            trackSet.initFromCarryOverContext(carryOverContext.getSubContext(subcontextName));
        }

    }

    private String getSubcontextName(PlayableCompositeTrack compositeTrack) {
        return "[MusicGrid." + compositeTrack.getTrackSetName().name() + "]";
    }

}
