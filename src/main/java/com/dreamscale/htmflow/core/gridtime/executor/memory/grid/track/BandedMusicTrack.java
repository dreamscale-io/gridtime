package com.dreamscale.htmflow.core.gridtime.executor.memory.grid.track;

import com.dreamscale.htmflow.core.gridtime.executor.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.executor.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.FeatureTag;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.FinishTag;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.StartTag;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.types.FinishTypeTag;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.types.StartTypeTag;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.cell.FeatureCell;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.cell.GridRow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
public class BandedMusicTrack<F extends FeatureReference> implements MusicTrack {

    private final MusicClock musicClock;
    private final String rowName;

    private Map<RelativeBeat, F> trackMusic = DefaultCollections.map();
    private MultiValueMap<RelativeBeat, FeatureTag<F>> trackTags = DefaultCollections.multiMap();

    public BandedMusicTrack(String rowName, MusicClock musicClock) {
        this.rowName = rowName;
        this.musicClock = musicClock;
    }

    public void initFirst(F feature) {
        if (feature != null) {
            startPlaying(musicClock.getStartBeat(), feature, StartTypeTag.Rollover);
        }
    }

    public void startPlaying(RelativeBeat fromBeat, F feature) {
        startPlaying(fromBeat, feature, StartTypeTag.Start);
    }

    public void startPlaying(RelativeBeat fromBeat, F feature, StartTag startTag) {
        F existingFeature = trackMusic.get(fromBeat);
        if (existingFeature != null) {
            log.warn("Overwriting existing music at beat: " + fromBeat.toDisplayString() + ": "
                    + existingFeature.toDisplayString() +" with "+feature.toDisplayString());
            removeFinishTags(fromBeat);
        }

        FeatureTag<F> lastStartTag = getLastStartTag(fromBeat);

        if (lastStartTag == null || lastStartTag.getFeature() != feature) {
            trackMusic.put(fromBeat, feature);
            trackTags.add(fromBeat, new FeatureTag<>(feature, startTag));
        }

        if (lastStartTag != null && lastStartTag.getFeature() == feature) {
            removeFinishTags(fromBeat);
        } else if (lastStartTag != null && lastStartTag.getFeature() != feature) {
            RelativeBeat prevBeat = musicClock.getPreviousBeat(fromBeat);
            trackTags.add(prevBeat, new FeatureTag<>(lastStartTag.getFeature(), FinishTypeTag.Success));
        }

    }

    private void removeFinishTags(RelativeBeat fromBeat) {

            List<FeatureTag<F>> tagsAtBeat = trackTags.get(fromBeat);
            if (tagsAtBeat != null && tagsAtBeat.size() > 0) {

                tagsAtBeat.removeIf(tag -> tag.getTag() instanceof FinishTag);
            }
    }

    public void stopPlaying(RelativeBeat toBeat) {
        stopPlaying(toBeat, FinishTypeTag.Success);
    }

    public void stopPlaying(RelativeBeat toBeat, FinishTag finishTag) {
        FeatureTag<F> startTag = getLastStartTag(toBeat);
        if (startTag != null) {
            fillBackwardUntilNotNull(toBeat, startTag.getFeature());
            if (finishTag != null) {
                trackTags.add(toBeat, new FeatureTag<>(startTag.getFeature(), finishTag));
            }
        }
    }

    @Override
    public GridRow toGridRow() {
        GridRow gridRow = new GridRow(rowName);

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
    public String getRowName() {
        return rowName;
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
            rolloverTag = new FeatureTag<>(featureToReplicate, StartTypeTag.Rollover);
        }

        return rolloverTag;
    }

    private void fillWithFeatureIfNull(RelativeBeat beat, F featureToReplicate) {
        if (trackMusic.get(beat) == null) {
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

    private FeatureTag<F> getLastStartTag(RelativeBeat searchBackwardsFromBeat) {

        Iterator<RelativeBeat> iterator = musicClock.getBackwardsIterator(searchBackwardsFromBeat);

        while (iterator.hasNext()) {
            RelativeBeat beat = iterator.next();

            List<FeatureTag<F>> tags = trackTags.get(beat);
            if (tags != null && tags.size() > 0) {
                for (FeatureTag<F> tag : tags) {
                    if (tag.isStart()) {
                        return tag;
                    }
                }
            }

        }

        return null;
    }


}
