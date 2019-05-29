package com.dreamscale.htmflow.core.gridtime.executor.memory.grid;

import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.returns.MusicGridResults;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.reference.*;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.cell.GridRow;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.track.*;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.trackset.*;
import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.IdeaFlowTile;
import com.dreamscale.htmflow.core.gridtime.executor.memory.FeatureCache;
import com.dreamscale.htmflow.core.gridtime.executor.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.executor.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.type.IdeaFlowStateType;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.type.WorkContextType;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.FinishTag;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.StartTag;
import com.dreamscale.htmflow.core.gridtime.executor.alarm.TimeBombTrigger;
import com.dreamscale.htmflow.core.gridtime.executor.alarm.Trigger;
import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.CarryOverContext;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public void startWorkContext(RelativeBeat beat, WorkContextReference workContext) {
        contextTracks.startPlaying(workContext.getWorkType(), beat, workContext);
    }

    public void endWorkContext(RelativeBeat beat, WorkContextReference workContext, FinishTag finishTag) {
        contextTracks.stopPlaying(workContext.getWorkType(), beat, finishTag);
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

    public WorkContextReference getFirstContextOfType(WorkContextType fromStructureLevel) {
        return contextTracks.getFirst(fromStructureLevel);
    }

    public WorkContextReference getLastContextOfTime(WorkContextType fromStructureLevel) {
        return contextTracks.getLast(fromStructureLevel);
    }

    public void startWTF(RelativeBeat beat, IdeaFlowStateReference wtfState, StartTag startTag) {
        ideaflowTracks.startWTF(beat, wtfState, startTag);
    }

    public void clearWTF(RelativeBeat beat, FinishTag finishTag) {
        ideaflowTracks.clearWTF(beat, finishTag);
    }

    public void timeBombContextEnding(TimeBombTrigger timeBombTrigger, WorkContextReference context, FinishTag finishTag) {
        timeBombTrigger.wireUpTrigger(new EndContextTrigger(timeBombTrigger.getFutureSplodingBeat(), context, finishTag));

        timeBombTriggers.add(timeBombTrigger);
    }

    public void gotoLocation(FeatureCache featureCache, RelativeBeat beat, PlaceReference locationInBox, Duration timeInLocation) {
        navigationTracks.gotoPlace(beat, locationInBox, timeInLocation);
    }

    public void modifyCurrentLocation(RelativeBeat beat, int modificationCount) {
        navigationTracks.modifyPlace(beat, modificationCount);
        ideaflowTracks.addModificationSampleForLearningBand(beat, modificationCount);
    }

    public void executeThing(RelativeBeat beat, ExecutionReference execution) {
        executionTracks.executeThing(beat, execution);
    }

    public MusicTrackSet<IdeaFlowStateType, IdeaFlowStateReference> play(IdeaFlowStateType ideaFlowState) {
        return null;
    }

    public IdeaFlowStateType getLastIdeaFlowState() {
        return null;
    }

    public MusicTrackSet<IdeaFlowStateType, IdeaFlowStateReference> getIdeaFlowMusic() {
        return null;
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
        List<String> rowsAsStrings = toMusicNotationRowsWithHeader(trackSet.toGridRows());

        return new MusicGridResults(rowsAsStrings);
    }

    public MusicGridResults playAllTracks() {
        List<String> rowsAsString = new ArrayList<>();

        for (TrackSetName trackName : trackSetsByName.keySet()) {
            PlayableCompositeTrack track = trackSetsByName.get(trackName);

            if (rowsAsString.isEmpty()) {
                rowsAsString.addAll(toMusicNotationRowsWithHeader(track.toGridRows()));
            } else {
                rowsAsString.addAll(toMusicNotationRows(track.toGridRows()));
            }
        }
        return new MusicGridResults(rowsAsString);
    }

    private List<String> toMusicNotationRows(List<GridRow> gridRows) {
        //first row is header row

        List<String> rows = DefaultCollections.list();

        if (gridRows != null && gridRows.size() > 0) {
            for (GridRow gridRow : gridRows) {
                rows.add( gridRow.getPrintedValueRow());
            }
        }

        return rows;
    }


    private List<String> toMusicNotationRowsWithHeader(List<GridRow> gridRows) {
        //first row is header row

        List<String> rows = DefaultCollections.list();

        if (gridRows != null && gridRows.size() > 0) {
            GridRow firstRow = gridRows.get(0);

            rows.add( firstRow.getPrintedHeaderRow());

            for (GridRow gridRow : gridRows) {
                rows.add( gridRow.getPrintedValueRow());
            }
        }

        return rows;
    }


    private List<GridRow> getPlayedTrackResults(TrackSetName trackSetName) {
        //todo implement one at a time, the context on some of these is a bit off

        if (trackSetName == contextTracks.getTrackSetName()) {
            return contextTracks.toGridRows();
        }
        return DefaultCollections.emptyList();
    }




    private class EndContextTrigger implements Trigger {
        private final WorkContextReference contextReference;
        private final FinishTag finishTag;
        private final RelativeBeat beat;

        public EndContextTrigger(RelativeBeat beat, WorkContextReference contextReference, FinishTag finishTag) {
            this.beat = beat;
            this.contextReference = contextReference;
            this.finishTag = finishTag;
        }

        public void fire() {
            endWorkContext(beat, contextReference, finishTag);
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
