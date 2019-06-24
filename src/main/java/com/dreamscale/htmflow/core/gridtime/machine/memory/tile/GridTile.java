package com.dreamscale.htmflow.core.gridtime.machine.memory.tile;

import com.dreamscale.htmflow.core.gridtime.machine.executor.alarm.TimeBombTrigger;
import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.machine.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.machine.clock.ZoomLevel;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tag.FinishTag;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tag.StartTag;
import com.dreamscale.htmflow.core.gridtime.machine.memory.type.WorkContextType;
import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.FeatureCache;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.details.*;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference.*;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.MusicGrid;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Slf4j
public class GridTile {

    private final UUID torchieId;
    private final GeometryClock.GridTime gridCoordinates;
    private final ZoomLevel zoomLevel;

    private final MusicClock musicClock;
    private final MusicGrid musicGrid;

    private transient FeatureCache featureCache;

    public GridTile(UUID torchieId, GeometryClock.GridTime gridCoordinates, FeatureCache featureCache) {
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

    public void startWorkContext(LocalDateTime moment, WorkContextEvent workContextEvent) {
        WorkContextReference projectReference = featureCache.lookupContextReference(StructureLevel.PROJECT,
                workContextEvent.getProjectId(), workContextEvent.getProjectName());

        WorkContextReference taskReference = featureCache.lookupContextReference(StructureLevel.TASK,
                workContextEvent.getTaskId(), workContextEvent.getTaskName());

        WorkContextReference intentionReference = featureCache.lookupContextReference(StructureLevel.INTENTION,
                workContextEvent.getIntentionId(), workContextEvent.getDescription());

        RelativeBeat beat = musicClock.getClosestBeat(gridCoordinates.getRelativeTime(moment));

        musicGrid.startWorkContext(beat, projectReference);
        musicGrid.startWorkContext(beat, taskReference);
        musicGrid.startWorkContext(beat, intentionReference);
    }

    /**
     * Change the active context, such as starting project, task, or intention
     */

    public void clearWorkContext(LocalDateTime moment, FinishTag finishTag) {

        Duration relativeTime = gridCoordinates.getRelativeTime(moment);
        if (musicClock.isWithinMeasure(relativeTime)) {
            RelativeBeat beat = musicClock.getClosestBeat(gridCoordinates.getRelativeTime(moment));

            musicGrid.clearWorkContext(beat, finishTag);
        } else {
            TimeBombTrigger timeBomb = musicClock.getFutureTimeBomb(gridCoordinates.getRelativeTime(moment));

            musicGrid.timeBombFutureContextEnding(timeBomb, finishTag);
        }
    }

    /**
     * Starts a WTF friction band...
     */
    public void startWTF(LocalDateTime moment, CircleDetails circleDetails, StartTag startTag) {
        IdeaFlowStateReference stateReference = featureCache.lookupWTFReference(circleDetails);

        RelativeBeat beat = musicClock.getClosestBeat(gridCoordinates.getRelativeTime(moment));

        musicGrid.startWTF(beat, stateReference, startTag);
    }

    /**
     * Clears a WTF friction band...
     */
    public void clearWTF(LocalDateTime endBandPosition, FinishTag finishTag) {
        RelativeBeat beat = musicClock.getClosestBeat(gridCoordinates.getRelativeTime(endBandPosition));

        musicGrid.clearWTF(beat, finishTag);
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

        WorkContextReference projectContext = musicGrid.getLastContextOnOrBeforeBeat(beat, WorkContextType.PROJECT_WORK);

        if (projectContext == null) {
            projectContext = featureCache.lookupDefaultProject();
            log.warn("Unable to identify projectId to lookup location, using default project");
        }

        PlaceReference locationInBox = featureCache.lookupLocationReference(projectContext.getReferenceId(), locationPath);

        musicGrid.gotoLocation(beat, locationInBox, timeInLocation);

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


    public Set<FeatureReference> getFeatures() {
        return musicGrid.getAllFeatures();
    }
}
