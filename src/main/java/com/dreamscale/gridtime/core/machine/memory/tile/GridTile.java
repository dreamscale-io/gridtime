package com.dreamscale.gridtime.core.machine.memory.tile;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.MusicGridResults;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.MusicClock;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.executor.circuit.alarm.TimeBomb;
import com.dreamscale.gridtime.core.machine.memory.box.TeamBoxConfiguration;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.feature.details.*;
import com.dreamscale.gridtime.core.machine.memory.feature.id.TeamHashId;
import com.dreamscale.gridtime.core.machine.memory.feature.id.TorchieHashId;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.*;
import com.dreamscale.gridtime.core.machine.memory.grid.IMusicGrid;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.GridRow;
import com.dreamscale.gridtime.core.machine.memory.grid.MusicGrid;
import com.dreamscale.gridtime.core.machine.memory.grid.query.metrics.BoxMetrics;
import com.dreamscale.gridtime.core.machine.memory.grid.query.metrics.IdeaFlowMetrics;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.TrackSetKey;
import com.dreamscale.gridtime.core.machine.memory.tag.FinishTag;
import com.dreamscale.gridtime.core.machine.memory.tag.StartTag;
import com.dreamscale.gridtime.core.machine.memory.type.CmdType;
import com.dreamscale.gridtime.core.machine.memory.type.WorkContextType;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
public class GridTile {

    private final UUID torchieId;
    private final GeometryClock.GridTime gridTime;
    private final ZoomLevel zoomLevel;

    private final MusicClock musicClock;
    private final MusicGrid musicGrid;
    private final TileAnalytics tileAnalytics;
    private final TeamBoxConfiguration teamBoxConfiguration;

    private transient FeatureCache featureCache;

    private final List<TimeBomb> timeBombTriggers;

    public GridTile(UUID torchieId, GeometryClock.GridTime gridTime, FeatureCache featureCache,
                    TeamBoxConfiguration teamBoxConfiguration) {
        this.torchieId = torchieId;
        this.gridTime = gridTime;
        this.featureCache = featureCache;
        this.teamBoxConfiguration = teamBoxConfiguration;

        this.zoomLevel = gridTime.getZoomLevel();
        this.musicClock = new MusicClock(zoomLevel);

        TorchieHashId torchieHashId = new TorchieHashId(torchieId);

        String title = "GridTile:Id:@tile"+ torchieHashId.toDisplayString() + gridTime.toDisplayString();
        this.musicGrid = new MusicGrid(title, featureCache, gridTime, musicClock);
        this.tileAnalytics = new TileAnalytics(featureCache, musicClock, musicGrid);

        this.timeBombTriggers = DefaultCollections.list();
    }

    /**
     * Change the active work context, such as starting project, task, or intention
     */

    public void startWorkContext(LocalDateTime moment, WorkContextEvent workContextEvent) {
        WorkContextReference projectReference = featureCache.lookupContextReference(StructureLevel.PROJECT,
                workContextEvent.getProjectId(), workContextEvent.getProjectName());

        WorkContextReference taskReference = featureCache.lookupContextReference(StructureLevel.TASK,
                workContextEvent.getTaskId(), workContextEvent.getTaskName());

        WorkContextReference intentionReference = featureCache.lookupContextReference(StructureLevel.INTENTION,
                workContextEvent.getIntentionId(), workContextEvent.getDescription());


        musicGrid.startWorkContext(moment, projectReference);
        musicGrid.startWorkContext(moment, taskReference);
        musicGrid.startWorkContext(moment, intentionReference);
    }

    /**
     * Clear the active work context, will clear project, task, and intention
     */

    public void clearWorkContext(LocalDateTime moment, FinishTag finishTag) {

        Duration relativeTime = gridTime.getRelativeTime(moment);
        if (musicClock.isWithinMeasure(relativeTime)) {
            musicGrid.clearWorkContext(moment, finishTag);
        } else {
            TimeBomb timeBomb = musicClock.getFutureTimeBomb(gridTime.getRelativeTime(moment));
            timeBomb.addOnAlarmInstruction(CmdType.END_WORK_CONTEXT, "tagName", finishTag.name());

            timeBombTriggers.add(timeBomb);
        }
    }

    /**
     * Starts a WTF friction band...
     */
    public void startWTF(LocalDateTime moment, CircleDetails circleDetails, StartTag startTag) {
        IdeaFlowStateReference stateReference = featureCache.lookupWTFReference(circleDetails);

        musicGrid.startWTF(moment, stateReference, startTag);
    }

    /**
     * Clears a WTF friction band...
     */
    public void clearWTF(LocalDateTime moment, FinishTag finishTag) {

        musicGrid.clearWTF(moment, finishTag);
    }

    /**
     * An alternative set of authors is active for all subsequent data until changed
     */
    public void startAuthors(LocalDateTime moment, AuthorsDetails authorsDetails) {
        AuthorsReference authorsReference = featureCache.lookupAuthorsReference(authorsDetails);

        musicGrid.startAuthors(moment, authorsReference);
    }

    /**
     * Clears out the current authors band, going back to default
     */
    public void clearAuthors(LocalDateTime moment) {

        musicGrid.clearAuthors(moment);
    }

    /**
     * Activate the feels flame rating from this point forward until cleared or changed
     */
    public void startFeelsBand(LocalDateTime moment, Integer feelsRating) {
        FeelsReference feelsState = featureCache.lookupFeelsStateReference(feelsRating);

        musicGrid.startFeels(moment, feelsState);
    }

    /**
     * Clear out the feels flame rating, and go back to default
     */
    public void clearFeelsBand(LocalDateTime moment) {

        musicGrid.clearFeels(moment);
    }

    /**
     * Walk through a sequence of locations in time and space, will map the rhythm of the transitions,
     * the frequency of traversals, and build a model of the connected software space based on visiting activity.
     * All the movements can be played back like music.
     */

    public void gotoLocation(LocalDateTime moment, String locationPath, Duration timeInLocation) {

        WorkContextReference projectContext = musicGrid.getLastContextOnOrBeforeMoment(moment, WorkContextType.PROJECT_WORK);

        if (projectContext == null) {
            projectContext = featureCache.lookupDefaultProject();
            log.warn("Unable to identify projectId to lookup location, using default project");
        }

        String box = teamBoxConfiguration.identifyBox(projectContext.getReferenceId(), locationPath);
        PlaceReference locationInBox = featureCache.lookupLocationReference(projectContext.getReferenceId(), box, locationPath);

        musicGrid.gotoLocation(moment, locationInBox, timeInLocation);

    }

    /**
     * Modification activity is aggregated to get an overall idea of how much modification is happening
     */

    public void modifyCurrentLocation(LocalDateTime moment, int modificationCount) {

        musicGrid.modifyCurrentLocation(moment, modificationCount);
    }

    /**
     * Walk through a sequence of executions of tests, builds, and firing up applications.  Look for
     * rhythms in the red/green patterns and cycle times.
     */
    public void executeThing(ExecutionEvent executionEvent) {

        ExecutionReference executionReference = featureCache.lookupExecutionReference(executionEvent);

        musicGrid.executeThing(executionReference);
    }

    public void finishAfterLoad() {
        musicGrid.finish();

        tileAnalytics.runMetrics();
    }

    public IdeaFlowMetrics getIdeaFlowMetrics() {
        return tileAnalytics.getIdeaFlowMetrics();
    }

    public List<BoxMetrics> getBoxMetrics() {
        return tileAnalytics.getBoxMetrics();
    }

    public CarryOverContext getCarryOverContext() {
        return musicGrid.getCarryOverContext();
    }

    public void initFromCarryOverContext(CarryOverContext carryOverContext) {
        if (carryOverContext != null) {
            musicGrid.initFromCarryOverContext(carryOverContext);
        }
    }

    public GeometryClock.GridTime getGridTime() {
        return gridTime;
    }

    public ZoomLevel getZoomLevel() {
        return zoomLevel;
    }

    public List<GridRow> getAllGridRows() {
        return musicGrid.getAllGridRows();
    }

    public Set<FeatureReference> getAllFeatures() {
        return musicGrid.getAllFeatures();
    }

    public MusicGridResults playAllTracks() {
        return musicGrid.playAllTracks();
    }

    public MusicGridResults playTrack(TrackSetKey trackToPlay) {
        return musicGrid.playTrack(trackToPlay);
    }

    public IMusicGrid getMusicGrid() {
        return musicGrid;
    }
}
