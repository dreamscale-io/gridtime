package com.dreamscale.htmflow.core.gridtime.machine.memory.grid.track;

import com.dreamscale.htmflow.core.gridtime.machine.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.machine.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tag.FeatureTag;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.FeatureCell;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.GridRow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;

import java.util.*;

@Slf4j
public class RhythmMusicTrack<F extends FeatureReference> implements MusicTrack {

    protected final MusicClock musicClock;
    private final String rowName;

    private MultiValueMap<RelativeBeat, F> trackMusic = DefaultCollections.multiMap();
    private MultiValueMap<RelativeBeat, FeatureTag<F>> trackTags = DefaultCollections.multiMap();

    public RhythmMusicTrack(String rowName, MusicClock musicClock) {
        this.rowName = rowName;
        this.musicClock = musicClock;
    }

    public void playEventAtBeat(RelativeBeat beat, F event) {
        trackMusic.add(beat, event);
    }

    public F getLatestEventOnOrBeforeBeat(RelativeBeat beat) {
        F latestEvent = null;

        Iterator<RelativeBeat> iterator = musicClock.getBackwardsIterator(beat);

        while (iterator.hasNext()) {
            RelativeBeat prevBeat = iterator.next();
            List<F> eventsAtBeat = trackMusic.get(prevBeat);
            if (eventsAtBeat != null && eventsAtBeat.size() > 0) {
                latestEvent = eventsAtBeat.get(eventsAtBeat.size() - 1);
                break;
            }
        }

        return latestEvent;
    }

    public Set<? extends FeatureReference> getFeatures() {
        Set<FeatureReference> features = DefaultCollections.set();

        Iterator<RelativeBeat> iterator = musicClock.getForwardsIterator();

        while (iterator.hasNext()) {
            RelativeBeat beat = iterator.next();
            List<F> eventsAtBeat = trackMusic.get(beat);
            if (eventsAtBeat != null && eventsAtBeat.size() > 0) {
                features.addAll(eventsAtBeat);
            }
        }

        return features;
    }

    @Override
    public GridRow toGridRow() {

        GridRow gridRow = new GridRow(rowName);

        Iterator<RelativeBeat> beatIterator = musicClock.getForwardsIterator();

        while (beatIterator.hasNext()) {
            RelativeBeat beat = beatIterator.next();

            gridRow.add(new FeatureCell<>(beat, getAllFeaturesAtBeat(beat), getTagsAt(beat)));
        }

        return gridRow;
    }

    @Override
    public FeatureTag<F> finish() {
        return null;
    }

    @Override
    public F getFeatureAt(int beatNumber) {
        return getFirstFeatureAt(musicClock.getBeat(beatNumber));
    }

    @Override
    public F getFeatureAt(RelativeBeat beat) {
        return getFirstFeatureAt(beat);
    }

    @Override
    public List<FeatureTag<F>> getTagsAt(int beatNumber) {
        return getTagsAt(musicClock.getBeat(beatNumber));
    }

    @Override
    public List<FeatureTag<F>> getTagsAt(RelativeBeat beat) {
        return trackTags.get(beat);
    }

    @Override
    public String getRowName() {
        return rowName;
    }

    public F getFirst() {
        Iterator<RelativeBeat> iterator = musicClock.getForwardsIterator();

        while (iterator.hasNext()) {
            F feature = getFirstFeatureAt(iterator.next());
            if (feature != null) {
                return feature;
            }
        }
        return null;

    }

    public F getLast() {
        Iterator<RelativeBeat> iterator = musicClock.getBackwardsIterator();

        while (iterator.hasNext()) {
            F feature = getLastFeatureAt(iterator.next());
            if (feature != null) {
                return feature;
            }
        }

        return null;
    }

    public F getFirstFeatureAt(RelativeBeat beat) {
        List<F> featuresAtBeat = trackMusic.get(beat);
        if (featuresAtBeat != null && featuresAtBeat.size() > 0) {
            return featuresAtBeat.get(0);
        }

        return null;
    }

    public F getLastFeatureAt(RelativeBeat beat) {
        List<F> featuresAtBeat = trackMusic.get(beat);
        if (featuresAtBeat != null && featuresAtBeat.size() > 0) {
            return featuresAtBeat.get(featuresAtBeat.size() - 1);
        }

        return null;
    }


    public List<F> getAllFeaturesAtBeat(RelativeBeat beat) {
        return trackMusic.get(beat);
    }


    public void addTagAtBeat(RelativeBeat beat, FeatureTag<F> tag) {
        trackTags.add(beat, tag);
    }


    public FeatureTag<F> getLastTagWithType(RelativeBeat searchBackwardsFromBeat, Class<?> tagTypeClass) {

        Iterator<RelativeBeat> iterator = musicClock.getBackwardsIterator(searchBackwardsFromBeat);

        while (iterator.hasNext()) {
            RelativeBeat beat = iterator.next();

            List<FeatureTag<F>> tags = trackTags.get(beat);
            if (tags != null && tags.size() > 0) {
                for (FeatureTag<F> tag : tags) {
                    if (tagTypeClass.isInstance(tag.getTag())) {
                        return tag;
                    }
                }
            }

        }

        return null;
    }

    public GridRow toGridRow(Map<F, String> shortHandReferences) {
        GridRow gridRow = new GridRow(rowName);

        Iterator<RelativeBeat> iterator = musicClock.getForwardsIterator();
        while (iterator.hasNext()) {
            RelativeBeat beat = iterator.next();
            List<F> features =  getAllFeaturesAtBeat(beat);
            List<String> shortHandsForFeatures = getAllShorthandsForFeatures(features, shortHandReferences);

            FeatureCell<F> cell = new FeatureCell<>(beat, features, shortHandsForFeatures, getTagsAt(beat));
            gridRow.add(cell);
        }

        return gridRow;
    }

    private List<String> getAllShorthandsForFeatures(List<F> features, Map<F, String> shortHandReferences) {
        List<String> shorthands = new ArrayList<>();

        if (features != null) {
            for (F feature : features) {
                shorthands.add(shortHandReferences.get(feature));
            }
        }

        return shorthands;
    }


}
