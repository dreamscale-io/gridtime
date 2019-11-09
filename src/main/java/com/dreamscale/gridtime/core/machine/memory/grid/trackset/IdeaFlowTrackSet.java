package com.dreamscale.gridtime.core.machine.memory.grid.trackset;

import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.MusicClock;
import com.dreamscale.gridtime.core.machine.clock.RelativeBeat;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.GridMetrics;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.FeatureRowKey;
import com.dreamscale.gridtime.core.machine.memory.tag.FinishTag;
import com.dreamscale.gridtime.core.machine.memory.tag.StartTag;
import com.dreamscale.gridtime.core.machine.memory.type.IdeaFlowStateType;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.IdeaFlowStateReference;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.AggregateType;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.GridRow;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.MetricRowKey;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.RollingAggregate;
import com.dreamscale.gridtime.core.machine.memory.grid.track.BandedMusicTrack;
import com.dreamscale.gridtime.core.machine.memory.grid.track.PlayableCompositeTrackSet;
import com.dreamscale.gridtime.core.machine.memory.grid.track.RollingAggregateTrack;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.TrackSetKey;
import com.dreamscale.gridtime.core.machine.memory.tile.CarryOverContext;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Slf4j
public class IdeaFlowTrackSet implements PlayableCompositeTrackSet {

    private final GeometryClock.GridTime gridTime;
    private final MusicClock musicClock;
    private final TrackSetKey trackSetKey;
    private final FeatureCache featureCache;

    private BandedMusicTrack<IdeaFlowStateReference> wtfTrack;
    private BandedMusicTrack<IdeaFlowStateReference> learningProgressTrack;

    private RollingAggregateTrack rollingAggregateModificationsTrack;

    private static final int MODIFICATION_THRESHOLD_FOR_PROGRESS = 150;

    public IdeaFlowTrackSet(TrackSetKey trackSetKey, FeatureCache featureCache, GeometryClock.GridTime gridTime, MusicClock musicClock) {
        this.trackSetKey = trackSetKey;
        this.featureCache = featureCache;
        this.gridTime = gridTime;
        this.musicClock = musicClock;

        this.wtfTrack = new BandedMusicTrack<>(FeatureRowKey.FLOW_WTF, gridTime, musicClock);
        this.learningProgressTrack = new BandedMusicTrack<>(FeatureRowKey.FLOW_LEARNING, gridTime, musicClock);

        this.rollingAggregateModificationsTrack = new RollingAggregateTrack(gridTime, musicClock);
    }

    public void startWTF(LocalDateTime moment, IdeaFlowStateReference wtfState, StartTag startTag) {
        wtfTrack.startPlaying(moment, wtfState, startTag);
    }

    public void clearWTF(LocalDateTime moment, FinishTag finishTag) {
        wtfTrack.stopPlaying(moment, finishTag);
    }

    public void addModificationSampleForLearningBand(RelativeBeat beat, int modificationSample) {
        rollingAggregateModificationsTrack.addModificationSample(beat, modificationSample);
    }

    @Override
    public void finish() {
        wtfTrack.finish();

        rollingAggregateModificationsTrack.finish();
        createLearningBandsBasedOnThreshold();

        learningProgressTrack.finish();
    }

    public IdeaFlowStateReference getIdeaFlowStateAtBeat(RelativeBeat beat) {
        IdeaFlowStateReference stateReference = wtfTrack.getFeatureAt(beat);
        if (stateReference == null) {
            stateReference = learningProgressTrack.getFeatureAt(beat);
        }

        return stateReference;
    }

    private void createLearningBandsBasedOnThreshold() {
        IdeaFlowStateReference progressState = featureCache.lookupIdeaFlowStateReference(IdeaFlowStateType.PROGRESS_STATE);
        IdeaFlowStateReference learningState = featureCache.lookupIdeaFlowStateReference(IdeaFlowStateType.LEARNING_STATE);

        Iterator<RelativeBeat> iterator = musicClock.toSummaryClock().getForwardsIterator();

        while (iterator.hasNext()) {
            RelativeBeat summaryBeat = iterator.next();
            RollingAggregate aggregate = rollingAggregateModificationsTrack.getAggregateAt(summaryBeat);

            LocalDateTime moment = gridTime.getMomentFromOffset(summaryBeat.getRelativeDuration());

            if (aggregate.isTotalOverThreshold(MODIFICATION_THRESHOLD_FOR_PROGRESS)) {
                learningProgressTrack.startPlaying(moment, progressState);
            } else {
                learningProgressTrack.startPlaying(moment, learningState);
            }
        }
    }



    public CarryOverContext getCarryOverContext(String subcontextName) {

        CarryOverContext carryOverContext = new CarryOverContext(subcontextName);

        //TODO this needs to not rollover on a terminated WTF or learning

        IdeaFlowStateReference lastWtf = wtfTrack.getLast();
        carryOverContext.saveReference("last.wtf", lastWtf);

        IdeaFlowStateReference lastLearning = learningProgressTrack.getLast();
        carryOverContext.saveReference("last.learning", lastLearning);

        carryOverContext.saveRollingAggregate("last.rolling.aggregate", rollingAggregateModificationsTrack.getLast());

        return carryOverContext;
    }

    public void initFromCarryOverContext(CarryOverContext subContext) {
        IdeaFlowStateReference lastWtf = (IdeaFlowStateReference) subContext.getReference("last.wtf");
        wtfTrack.initFirst(lastWtf);

        IdeaFlowStateReference lastLearning = (IdeaFlowStateReference) subContext.getReference("last.learning");
        learningProgressTrack.initFirst(lastLearning);

        RollingAggregate aggregate = subContext.getRollingAggregate("last.rolling.aggregate");
        rollingAggregateModificationsTrack.initCarryOver(aggregate);
    }

    @Override
    public void populateBoxWithBeat(RelativeBeat beat, GridMetrics boxMetrics) {

        IdeaFlowStateReference ideaFlowState = getIdeaFlowStateAtBeat(beat);
        if (ideaFlowState != null) {
            if (ideaFlowState.getIdeaFlowStateType() == IdeaFlowStateType.WTF_STATE) {
                boxMetrics.addWtfSample(true);
                boxMetrics.addLearningSample(false);
                boxMetrics.addProgressSample(false);
            } else if (ideaFlowState.getIdeaFlowStateType() == IdeaFlowStateType.LEARNING_STATE) {
                boxMetrics.addWtfSample(false);
                boxMetrics.addLearningSample(true);

            } else if (ideaFlowState.getIdeaFlowStateType() == IdeaFlowStateType.PROGRESS_STATE) {
                boxMetrics.addWtfSample(false);
                boxMetrics.addLearningSample(false);
                boxMetrics.addProgressSample(true);
            }
        }
    }

    @Override
    public TrackSetKey getTrackSetKey() {
        return trackSetKey;
    }

    @Override
    public List<GridRow> toGridRows() {
        List<GridRow> rows = new ArrayList<>();
        rows.add(wtfTrack.toGridRow());
        rows.add(learningProgressTrack.toGridRow());
        rows.add(rollingAggregateModificationsTrack.toGridRow(MetricRowKey.FLOW_MODS, AggregateType.TOTAL));

        return rows;
    }

    public Set<? extends FeatureReference> getFeatures() {
        Set<FeatureReference> allFeatures = DefaultCollections.set();

        allFeatures.addAll(wtfTrack.getFeatures());
        allFeatures.addAll(learningProgressTrack.getFeatures());

        return allFeatures;
    }



}
