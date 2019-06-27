package com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.trackset;

import com.dreamscale.htmflow.core.gridtime.kernel.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tag.FinishTag;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tag.StartTag;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.type.IdeaFlowStateType;
import com.dreamscale.htmflow.core.gridtime.kernel.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.cache.FeatureCache;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.IdeaFlowStateReference;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.AggregateType;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.GridRow;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.MetricType;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.RollingAggregate;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track.BandedMusicTrack;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track.PlayableCompositeTrack;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track.RollingAggregateTrack;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track.TrackSetName;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tile.CarryOverContext;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Slf4j
public class IdeaFlowTrackSet implements PlayableCompositeTrack  {

    private final GeometryClock.GridTime gridTime;
    private final MusicClock musicClock;
    private final TrackSetName trackSetName;

    private BandedMusicTrack<IdeaFlowStateReference> wtfTrack;
    private BandedMusicTrack<IdeaFlowStateReference> learningProgressTrack;

    private RollingAggregateTrack rollingAggregateModificationsTrack;

    private static final int MODIFICATION_THRESHOLD_FOR_PROGRESS = 150;

    public IdeaFlowTrackSet(TrackSetName trackSetName, GeometryClock.GridTime gridTime, MusicClock musicClock) {
        this.trackSetName = trackSetName;
        this.gridTime = gridTime;
        this.musicClock = musicClock;

        this.wtfTrack = new BandedMusicTrack<>("@flow/wtf", gridTime, musicClock);
        this.learningProgressTrack = new BandedMusicTrack<>("@flow/learning", gridTime, musicClock);

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

    public void finish(FeatureCache featureCache) {
        wtfTrack.finish();

        rollingAggregateModificationsTrack.finish();
        createLearningBandsBasedOnThreshold(featureCache);

        learningProgressTrack.finish();
    }

    private void createLearningBandsBasedOnThreshold(FeatureCache featureCache) {
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
    public TrackSetName getTrackSetName() {
        return trackSetName;
    }

    @Override
    public List<GridRow> toGridRows() {
        List<GridRow> rows = new ArrayList<>();
        rows.add(wtfTrack.toGridRow());
        rows.add(learningProgressTrack.toGridRow());
        rows.add(rollingAggregateModificationsTrack.toGridRow(MetricType.FLOW_MODS, AggregateType.TOTAL));

        return rows;
    }

    public Set<? extends FeatureReference> getFeatures() {
        Set<FeatureReference> allFeatures = DefaultCollections.set();

        allFeatures.addAll(wtfTrack.getFeatures());
        allFeatures.addAll(learningProgressTrack.getFeatures());

        return allFeatures;
    }



}
