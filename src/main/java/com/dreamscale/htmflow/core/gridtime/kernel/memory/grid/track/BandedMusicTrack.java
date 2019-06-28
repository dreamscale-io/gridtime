package com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track;

import com.dreamscale.htmflow.core.gridtime.kernel.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.query.key.FeatureRowKey;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.query.key.Key;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tag.FeatureTag;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tag.FinishTag;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tag.StartTag;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tag.types.FinishTypeTag;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tag.types.StartTypeTag;
import com.dreamscale.htmflow.core.gridtime.kernel.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.type.FeatureCell;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.GridRow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public class BandedMusicTrack<F extends FeatureReference> implements MusicTrack {

    private final MusicClock musicClock;
    private final GeometryClock.GridTime gridTime;
    private final FeatureRowKey rowKey;

    private Map<RelativeBeat, F> trackMusic = DefaultCollections.map();
    private MultiValueMap<RelativeBeat, FeatureTag<F>> trackTags = DefaultCollections.multiMap();

    public BandedMusicTrack(FeatureRowKey rowKey, GeometryClock.GridTime gridTime, MusicClock musicClock) {
        this.rowKey = rowKey;
        this.gridTime = gridTime;
        this.musicClock = musicClock;
    }

    public Set<? extends FeatureReference> getFeatures() {
        return new LinkedHashSet<>(trackMusic.values());
    }

    public void initFirst(F feature) {
        if (feature != null) {
            startPlaying(null, feature, StartTypeTag.Rollover);
        }
    }

    public void startPlaying(LocalDateTime moment, F feature) {
        startPlaying(moment, feature, StartTypeTag.Start);
    }

    public void startPlaying(LocalDateTime moment, F feature, StartTag startTag) {
        if (moment == null) {
            moment = gridTime.getClockTime();
        }

        RelativeBeat fromBeat = musicClock.getClosestBeat(gridTime.getRelativeTime(moment));

        F existingFeature = trackMusic.get(fromBeat);
        if (existingFeature != null) {
            log.warn("Overwriting existing music at beat: " + fromBeat.toDisplayString() + ": "
                    + existingFeature.toDisplayString() +" with "+feature.toDisplayString());
            removeFinishTags(fromBeat);
        }

        FeatureTag<F> lastStartTag = getLastStartTagInProgress(fromBeat);


        if (lastStartTag == null || lastStartTag.getFeature() != feature) {
            trackMusic.put(fromBeat, feature);
            trackTags.add(fromBeat, new FeatureTag<>(moment, feature, startTag));
        }

        if (lastStartTag != null && lastStartTag.getFeature() == feature) {
            removeFinishTags(fromBeat);
        } else if (lastStartTag != null && lastStartTag.getFeature() != feature) {
            RelativeBeat prevBeat = musicClock.getPreviousBeat(fromBeat);
            trackTags.add(prevBeat, new FeatureTag<>(moment, lastStartTag.getFeature(), FinishTypeTag.Success));
        }

    }

    private void removeFinishTags(RelativeBeat fromBeat) {

            List<FeatureTag<F>> tagsAtBeat = trackTags.get(fromBeat);
            if (tagsAtBeat != null && tagsAtBeat.size() > 0) {

                tagsAtBeat.removeIf(tag -> tag.getTag() instanceof FinishTag);
            }
    }

    public void stopPlaying(LocalDateTime moment) {
        stopPlaying(moment, FinishTypeTag.Success);
    }

    public void stopPlaying(LocalDateTime moment, FinishTag finishTag) {
        RelativeBeat toBeat = musicClock.getClosestBeat(gridTime.getRelativeTime(moment));

        FeatureTag<F> startTag = getLastStartTagInProgress(toBeat);
        if (startTag != null) {
            fillBackwardUntilNotNull(toBeat, startTag.getFeature());
            if (finishTag != null) {
                trackTags.add(toBeat, new FeatureTag<>(moment, startTag.getFeature(), finishTag));
            }
        }
    }

    @Override
    public GridRow toGridRow() {
        GridRow gridRow = new GridRow(rowKey);

        Iterator<RelativeBeat> beatIterator = musicClock.getForwardsIterator();

        while (beatIterator.hasNext()) {
            RelativeBeat beat = beatIterator.next();
            gridRow.add(new FeatureCell<>(beat, getFeatureAt(beat), getTagsAt(beat)));
        }
        return gridRow;
    }


    @Override
    public F getFirst() {
        Iterator<RelativeBeat> iterator = musicClock.getForwardsIterator();

        while (iterator.hasNext()) {
            F feature = getFeatureAt(iterator.next());
            if (feature != null) {
                return feature;
            }
        }
        return null;

    }

    @Override
    public F getLast() {
        Iterator<RelativeBeat> iterator = musicClock.getBackwardsIterator();

        while (iterator.hasNext()) {
            F feature = getFeatureAt(iterator.next());
            if (feature != null) {
                return feature;
            }
        }

        return null;
    }

    public F getLastOnOrBeforeBeat(RelativeBeat beat) {
        Iterator<RelativeBeat> iterator = musicClock.getBackwardsIterator(beat);

        while (iterator.hasNext()) {
            F feature = getFeatureAt(iterator.next());
            if (feature != null) {
                return feature;
            }
        }
        return null;
    }

    @Override
    public F getFeatureAt(int beatNumber) {
        RelativeBeat beat = musicClock.getBeat(beatNumber);
        return getFeatureAt(beat);
    }

    @Override
    public F getFeatureAt(RelativeBeat beat) {
        return trackMusic.get(beat);
    }

    @Override
    public List<FeatureTag<F>> getTagsAt(int beatNumber) {
        RelativeBeat beat = musicClock.getBeat(beatNumber);
        return getTagsAt(beat);
    }


    @Override
    public List<FeatureTag<F>> getTagsAt(RelativeBeat beat) {
        return trackTags.get(beat);
    }

    @Override
    public Key getRowKey() {
        return rowKey;
    }

    @Override
    public FeatureTag<F> finish() {
        //start from the beginning, look for start tags, then replicate content either, to the end,
        // or until hitting an end tag

        FeatureTag<F> rolloverToNextTile = fillForwardFromStartTags();
        removeTemporaryRolloverTags();

        return rolloverToNextTile;
    }

    private void removeTemporaryRolloverTags() {

        Iterator<RelativeBeat> beatIterator = musicClock.getForwardsIterator();

        while (beatIterator.hasNext()) {
            RelativeBeat beat = beatIterator.next();

            List<FeatureTag<F>> tagsAtBeat = trackTags.get(beat);
            if (tagsAtBeat != null && tagsAtBeat.size() > 0) {

                tagsAtBeat.removeIf(tag -> tag.getTag() == StartTypeTag.Rollover);
            }

        }
    }

    private FeatureTag<F> fillForwardFromStartTags() {

        F featureToReplicate = null;

        Iterator<RelativeBeat> iterator = musicClock.getForwardsIterator();

        while (iterator.hasNext()) {
            RelativeBeat nextBeat = iterator.next();

            fillWithFeatureIfNull(nextBeat, featureToReplicate);

            List<FeatureTag<F>> tagsAtBeat = trackTags.get(nextBeat);
            if (tagsAtBeat != null && tagsAtBeat.size() > 0) {
                for (FeatureTag<F> tag : tagsAtBeat) {
                    if (tag.isStart()) {
                        featureToReplicate = tag.getFeature();
                    }
                    if (tag.isFinish()) {
                        featureToReplicate = null;
                    }
                }
            }
        }

        FeatureTag<F> rolloverTag = null;
        if (featureToReplicate != null) {
            rolloverTag = new FeatureTag<>(null, featureToReplicate, StartTypeTag.Rollover);
        }

        return rolloverTag;
    }

    private void fillWithFeatureIfNull(RelativeBeat beat, F featureToReplicate) {
        if (trackMusic.get(beat) == null && featureToReplicate != null) {
            trackMusic.put(beat, featureToReplicate);
        }
    }


    private void fillBackwardUntilNotNull(RelativeBeat backwardFromBeat, F feature) {

        Iterator<RelativeBeat> iterator = musicClock.getBackwardsIterator(backwardFromBeat);

        while ( iterator.hasNext() ) {
            RelativeBeat prevBeat = iterator.next();
            if (trackMusic.get(prevBeat) == null) {
                trackMusic.put(prevBeat, feature);
            } else {
                break;
            }
        }
    }

    private FeatureTag<F> getLastStartTagInProgress(RelativeBeat searchBackwardsFromBeat) {

        Iterator<RelativeBeat> iterator = musicClock.getBackwardsIterator(searchBackwardsFromBeat);

        while (iterator.hasNext()) {
            RelativeBeat beat = iterator.next();

            List<FeatureTag<F>> tags = trackTags.get(beat);
            if (tags != null && tags.size() > 0) {
                for (FeatureTag<F> tag : tags) {
                    if (tag.isFinish() && !beat.equals(searchBackwardsFromBeat)) {
                        return null;
                    }
                    if (tag.isStart()) {
                        return tag;
                    }
                }
            }

        }

        return null;
    }



}
