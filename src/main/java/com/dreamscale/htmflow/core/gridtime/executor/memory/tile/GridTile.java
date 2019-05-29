package com.dreamscale.htmflow.core.gridtime.executor.memory.tile;

import com.dreamscale.htmflow.core.gridtime.executor.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.executor.clock.ZoomLevel;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.details.AuthorsDetails;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.details.ExecutionEvent;
import com.dreamscale.htmflow.core.gridtime.executor.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.executor.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.reference.*;
import com.dreamscale.htmflow.core.gridtime.executor.memory.FeatureCache;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.type.IdeaFlowStateType;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.type.WorkContextType;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.MusicGrid;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.FinishTag;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.types.FinishCircleTag;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.types.StartCircleTag;
import com.dreamscale.htmflow.core.gridtime.executor.alarm.TimeBombTrigger;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.details.StructureLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Slf4j
public class GridTile {

    private final UUID torchieId;
    private final GeometryClock.Coords gridCoordinates;
    private final ZoomLevel zoomLevel;

    private final MusicClock musicClock;
    private final MusicGrid musicGrid;

    private transient FeatureCache featureCache;

    public GridTile(UUID torchieId, GeometryClock.Coords gridCoordinates, FeatureCache featureCache) {
        this.torchieId = torchieId;
        this.gridCoordinates = gridCoordinates;
        this.zoomLevel = gridCoordinates.getZoomLevel();
        this.musicClock = new MusicClock(zoomLevel);

        this.musicGrid = new MusicGrid(musicClock);
        this.featureCache = featureCache;
    }


    /**
     * Change the active context, such as starting project, task, or intention
     */

    public void beginContext(LocalDateTime moment, StructureLevel structureLevel, UUID referenceId, String description) {
        WorkContextReference contextReference = featureCache.lookupContextReference(structureLevel, referenceId, description);

        RelativeBeat beat = musicClock.getClosestBeat(gridCoordinates.getRelativeTime(moment));

        musicGrid.startWorkContext(beat, contextReference);
    }

    /**
     * Change the active context, such as starting project, task, or intention
     */

    public void endContext(LocalDateTime moment, StructureLevel structureLevel, UUID referenceId, String description, FinishTag finishTag) {
        WorkContextReference contextReference = featureCache.lookupContextReference(structureLevel, referenceId, description);

        Duration relativeTime = gridCoordinates.getRelativeTime(moment);
        if (musicClock.isWithinMeasure(relativeTime)) {
            RelativeBeat beat = musicClock.getClosestBeat(gridCoordinates.getRelativeTime(moment));

            musicGrid.endWorkContext(beat, contextReference, finishTag);
        } else {
            TimeBombTrigger futureTimeBombTrigger = musicClock.getFutureTimeBomb(gridCoordinates.getRelativeTime(moment));

            musicGrid.timeBombContextEnding(futureTimeBombTrigger, contextReference, finishTag);
        }
    }

    public WorkContextReference getFirstContext(StructureLevel structureLevel) {
        return musicGrid.getFirstContextOfType(WorkContextType.fromLevel(structureLevel));
    }

    public WorkContextReference getLastContext(StructureLevel structureLevel) {
        return musicGrid.getLastContextOfTime(WorkContextType.fromLevel(structureLevel));
    }

    /**
     * Starts a WTF friction band...
     */
    public void startWTF(LocalDateTime moment, StartCircleTag startCircleTag) {
        IdeaFlowStateReference stateReference = featureCache.lookupIdeaFlowStateReference(IdeaFlowStateType.WTF_STATE);

        RelativeBeat beat = musicClock.getClosestBeat(gridCoordinates.getRelativeTime(moment));

        musicGrid.startWTF(beat, stateReference, startCircleTag);
    }

    /**
     * Clears a WTF friction band...
     */
    public void clearWTF(LocalDateTime endBandPosition, FinishCircleTag finishCircleTag) {
        RelativeBeat beat = musicClock.getClosestBeat(gridCoordinates.getRelativeTime(endBandPosition));

        musicGrid.clearWTF(beat, finishCircleTag);
    }

    /**
     * An alternative set of authors is active for all subsequent data until changed
     *
     * @param startBandPosition
     */
    public void startAuthors(LocalDateTime startBandPosition, AuthorsDetails authorsDetails) {
        AuthorsReference authorsReference = featureCache.lookupAuthorsReference(authorsDetails);

        RelativeBeat beat = musicClock.getClosestBeat(gridCoordinates.getRelativeTime(startBandPosition));

        musicGrid.startAuthors(beat, authorsReference);
    }

    /**
     * Clears a WTF friction band...
     */
    public void clearAuthors(LocalDateTime endBandPosition) {
        RelativeBeat beat = musicClock.getClosestBeat(gridCoordinates.getRelativeTime(endBandPosition));

        musicGrid.clearAuthors(beat);
    }

    /**
     * Activate the feels flame rating from this point forward until cleared or changed
     *
     * @param startBandPosition
     * @param feelsRating
     */
    public void startFeelsBand(LocalDateTime startBandPosition, Integer feelsRating) {
        FeelsReference feelsState = featureCache.lookupFeelsStateReference(feelsRating);

        RelativeBeat beat = musicClock.getClosestBeat(gridCoordinates.getRelativeTime(startBandPosition));

        musicGrid.startFeels(beat, feelsState);
    }

    /**
     * Clear out the feels flame rating, and go back to default
     *
     * @param endBandPosition
     */
    public void clearFeelsBand(LocalDateTime endBandPosition) {

        RelativeBeat beat = musicClock.getClosestBeat(gridCoordinates.getRelativeTime(endBandPosition));

        musicGrid.clearFeels(beat);

    }

    //next thing I need to do is location mappings


    /**
     * Walk through a sequence of events, and the StoryFrame will build a model of locations in space,
     * and movements through time, that can be played back like music
     */

    public void gotoLocation(LocalDateTime moment, String locationPath, Duration timeInLocation) {

        RelativeBeat beat = musicClock.getClosestBeat(gridCoordinates.getRelativeTime(moment));

        WorkContextReference projectContext = musicGrid.getLastContextOfTime(WorkContextType.PROJECT_WORK);

        if (projectContext == null) {
            projectContext = featureCache.lookupDefaultProject();
            log.warn("Unable to identify projectId to lookup location, using default project");
        }

        PlaceReference locationInBox = featureCache.lookupLocationReference(projectContext.getReferenceId(), locationPath);

        musicGrid.gotoLocation(featureCache, beat, locationInBox, timeInLocation);

    }

    /**
     * Modification activity is aggregated to get an overall idea of how much modification is happening
     */

    public void modifyCurrentLocation(LocalDateTime moment, int modificationCount) {

        RelativeBeat beat = musicClock.getClosestBeat(gridCoordinates.getRelativeTime(moment));

        musicGrid.modifyCurrentLocation(beat, modificationCount);
    }


    /**
     * Execution rhythms are mapped by the FlowRhythmMapper to look for patterns of red/green bar
     * changes, and patterns in execution cycles.  Execution times are aggregated across focus areas
     *
     * @param moment
     * @param executionEvent
     */
    public void executeThing(LocalDateTime moment, ExecutionEvent executionEvent) {

        ExecutionReference executionReference = featureCache.lookupExecutionReference(executionEvent);
        RelativeBeat beat = musicClock.getClosestBeat(gridCoordinates.getRelativeTime(moment));

        musicGrid.executeThing(beat, executionReference);

    }


    public CarryOverContext getCarryOverContext() {
        return musicGrid.getCarryOverContext();
    }

    public void initFromCarryOverContext(CarryOverContext carryOverContext) {
        if (carryOverContext != null) {
            musicGrid.initFromCarryOverContext(carryOverContext);
        }
    }

    public void finishAfterLoad() {
        musicGrid.finish(featureCache);
    }

    public IdeaFlowTile getIdeaFlowTile() {
        return musicGrid.getIdeaFlowTile();
    }
}
