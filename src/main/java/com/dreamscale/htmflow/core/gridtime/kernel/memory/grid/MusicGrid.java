package com.dreamscale.htmflow.core.gridtime.kernel.memory.grid;

import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.returns.MusicGridResults;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.*;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.GridRow;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track.*;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.trackset.*;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tile.IdeaFlowTile;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.cache.FeatureCache;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.kernel.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.type.WorkContextType;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tag.FinishTag;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tag.StartTag;
import com.dreamscale.htmflow.core.gridtime.kernel.executor.circuit.alarm.TimeBombTrigger;
import com.dreamscale.htmflow.core.gridtime.kernel.executor.circuit.alarm.Trigger;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tile.CarryOverContext;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class MusicGrid {

    private final MusicClock musicClock;


    private AuthorsTrackSet authorsTracks;
    private FeelsTrackSet feelsTracks;

    private WorkContextTrackSet contextTracks;
    private IdeaFlowTrackSet ideaflowTracks;

    private ExecutionTrackSet executionTracks;
    private NavigationTrackSet navigationTracks;

    private List<TimeBombTrigger> timeBombTriggers = DefaultCollections.list();

    private Map<TrackSetName, PlayableCompositeTrack> trackSetsByName = DefaultCollections.map();

    public MusicGrid(MusicClock musicClock) {
        this.musicClock = musicClock;

        this.authorsTracks = new AuthorsTrackSet(TrackSetName.Authors, musicClock);
        this.feelsTracks = new FeelsTrackSet(TrackSetName.Feels, musicClock);

        this.contextTracks = new WorkContextTrackSet(TrackSetName.WorkContext, musicClock);
        this.ideaflowTracks = new IdeaFlowTrackSet(TrackSetName.IdeaFlow, musicClock);

        this.executionTracks = new ExecutionTrackSet(TrackSetName.Executions, musicClock);
        this.navigationTracks = new NavigationTrackSet(TrackSetName.Navigations, musicClock);

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

    public void startWorkContext(RelativeBeat beat, WorkContextReference workContext) {
        contextTracks.startWorkContext(beat, workContext);
    }

    public void clearWorkContext(RelativeBeat beat, FinishTag finishTag) {
        contextTracks.clearWorkContext(beat, finishTag);
    }

    public void startAuthors(RelativeBeat beat, AuthorsReference authorsReference) {
        authorsTracks.startAuthors(beat, authorsReference);
    }

    public void clearAuthors(RelativeBeat beat) {
        authorsTracks.clearAuthors(beat);
    }


    public void startFeels(RelativeBeat beat, FeelsReference feelsState) {
        feelsTracks.startFeels(beat, feelsState);
    }

    public void clearFeels(RelativeBeat beat) {
        feelsTracks.clearFeels(beat);
    }

    public WorkContextReference getLastContextOnOrBeforeBeat(RelativeBeat beat, WorkContextType fromStructureLevel) {
        return contextTracks.getLastOnOrBeforeBeat(beat, fromStructureLevel);
    }

    public void startWTF(RelativeBeat beat, IdeaFlowStateReference wtfState, StartTag startTag) {
        ideaflowTracks.startWTF(beat, wtfState, startTag);
    }

    public void clearWTF(RelativeBeat beat, FinishTag finishTag) {
        ideaflowTracks.clearWTF(beat, finishTag);
    }

    public void timeBombFutureContextEnding(TimeBombTrigger timeBombTrigger, FinishTag finishTag) {
        timeBombTrigger.wireUpTrigger(new EndContextTrigger(timeBombTrigger.getFutureSplodingBeat(), finishTag));

        timeBombTriggers.add(timeBombTrigger);
    }

    public void gotoLocation(RelativeBeat beat, PlaceReference locationInBox, Duration timeInLocation) {
        navigationTracks.gotoPlace(beat, locationInBox, timeInLocation);
    }

    public void modifyCurrentLocation(RelativeBeat beat, int modificationCount) {
        navigationTracks.modifyPlace(beat, modificationCount);
        ideaflowTracks.addModificationSampleForLearningBand(beat, modificationCount);
    }

    public void executeThing(RelativeBeat beat, ExecutionReference execution) {
        executionTracks.executeThing(beat, execution);
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
        List<GridRow> allRows = new ArrayList<>();

        for (TrackSetName trackName : trackSetsByName.keySet()) {
            PlayableCompositeTrack track = trackSetsByName.get(trackName);

            allRows.addAll(track.toGridRows());
        }

        return toMusicGridResults(allRows);
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


    private class EndContextTrigger implements Trigger {
        private final FinishTag finishTag;
        private final RelativeBeat beat;

        public EndContextTrigger(RelativeBeat beat, FinishTag finishTag) {
            this.beat = beat;
            this.finishTag = finishTag;
        }

        public void fire() {
            clearWorkContext(beat, finishTag);
        }
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
