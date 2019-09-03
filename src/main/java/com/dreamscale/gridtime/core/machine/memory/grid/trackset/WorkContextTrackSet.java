package com.dreamscale.gridtime.core.machine.memory.grid.trackset;

import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.MusicClock;
import com.dreamscale.gridtime.core.machine.clock.RelativeBeat;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.GridRow;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.GridMetrics;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.FeatureRowKey;
import com.dreamscale.gridtime.core.machine.memory.grid.track.BandedMusicTrack;
import com.dreamscale.gridtime.core.machine.memory.grid.track.PlayableCompositeTrackSet;
import com.dreamscale.gridtime.core.machine.memory.tag.FinishTag;
import com.dreamscale.gridtime.core.machine.memory.type.WorkContextType;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.WorkContextReference;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.TrackSetKey;
import com.dreamscale.gridtime.core.machine.memory.tile.CarryOverContext;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Slf4j
public class WorkContextTrackSet implements PlayableCompositeTrackSet {

    private final TrackSetKey trackSetKey;
    private final GeometryClock.GridTime gridTime;
    private final MusicClock musicClock;

    private final BandedMusicTrack<WorkContextReference> projectTrack;
    private final BandedMusicTrack<WorkContextReference> taskTrack;
    private final BandedMusicTrack<WorkContextReference> intentionTrack;

    public WorkContextTrackSet(TrackSetKey trackSetKey, GeometryClock.GridTime gridTime, MusicClock musicClock) {
        this.trackSetKey = trackSetKey;
        this.gridTime = gridTime;
        this.musicClock = musicClock;

        this.projectTrack = new BandedMusicTrack<>(FeatureRowKey.WORK_PROJECT, gridTime, musicClock);
        this.taskTrack = new BandedMusicTrack<>(FeatureRowKey.WORK_TASK, gridTime, musicClock);
        this.intentionTrack = new BandedMusicTrack<>(FeatureRowKey.WORK_INTENTION, gridTime, musicClock);
    }

    public void startWorkContext(LocalDateTime moment, WorkContextReference workContext) {
        if (workContext.getWorkType() == WorkContextType.PROJECT_WORK) {
            projectTrack.startPlaying(moment, workContext);
        } else if (workContext.getWorkType() == WorkContextType.TASK_WORK) {
            taskTrack.startPlaying(moment, workContext);
        } else if (workContext.getWorkType() == WorkContextType.INTENTION_WORK) {
            intentionTrack.startPlaying(moment, workContext);
        }
    }

    public void clearWorkContext(LocalDateTime moment, FinishTag finishTag) {
        projectTrack.stopPlaying(moment, finishTag);
        taskTrack.stopPlaying(moment, finishTag);
        intentionTrack.stopPlaying(moment, finishTag);
    }

    public WorkContextReference getLastOnOrBeforeMoment(LocalDateTime moment, WorkContextType workContextType) {
        RelativeBeat beat = musicClock.getClosestBeat(gridTime.getRelativeTime(moment));

        if (workContextType == WorkContextType.PROJECT_WORK) {
            return projectTrack.getLastOnOrBeforeBeat(beat);
        } else if (workContextType == WorkContextType.TASK_WORK) {
            return taskTrack.getLastOnOrBeforeBeat(beat);
        } else if (workContextType == WorkContextType.INTENTION_WORK) {
            return intentionTrack.getLastOnOrBeforeBeat(beat);
        }
        return null;
    }

    public CarryOverContext getCarryOverContext(String subcontextName) {

        CarryOverContext carryOverContext = new CarryOverContext(subcontextName);

        WorkContextReference lastProject = projectTrack.getLast();
        WorkContextReference lastTask = taskTrack.getLast();
        WorkContextReference lastIntention = intentionTrack.getLast();

        carryOverContext.saveReference("last.project", lastProject);
        carryOverContext.saveReference("last.task", lastTask);
        carryOverContext.saveReference("last.intention", lastIntention);

        return carryOverContext;
    }

    public void initFromCarryOverContext(CarryOverContext carryOverContext) {

        WorkContextReference lastProject = carryOverContext.getReference("last.project");
        WorkContextReference lastTask = carryOverContext.getReference("last.task");
        WorkContextReference lastIntention = carryOverContext.getReference("last.intention");

        projectTrack.initFirst(lastProject);
        taskTrack.initFirst(lastTask);
        intentionTrack.initFirst(lastIntention);
    }

    @Override
    public void populateBoxWithBeat(RelativeBeat beat, GridMetrics boxMetrics) {

    }

    @Override
    public TrackSetKey getTrackSetKey() {
        return trackSetKey;
    }

    @Override
    public void finish() {
        projectTrack.finish();
        taskTrack.finish();
        intentionTrack.finish();
    }

    @Override
    public List<GridRow> toGridRows() {
        List<GridRow> gridRows = new ArrayList<>();
        gridRows.add(projectTrack.toGridRow());
        gridRows.add(taskTrack.toGridRow());
        gridRows.add(intentionTrack.toGridRow());

        return gridRows;
    }

    public Set<FeatureReference> getFeatures() {
        Set<FeatureReference> featureReferences = DefaultCollections.set();

        featureReferences.addAll(projectTrack.getFeatures());
        featureReferences.addAll(taskTrack.getFeatures());
        featureReferences.addAll(intentionTrack.getFeatures());

        return featureReferences;
    }



}
