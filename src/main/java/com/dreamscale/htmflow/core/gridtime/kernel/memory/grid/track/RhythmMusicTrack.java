package com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track;

import com.dreamscale.htmflow.core.gridtime.kernel.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.kernel.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tag.FeatureTag;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.type.FeatureCell;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.GridRow;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public class RhythmMusicTrack<F extends FeatureReference> implements MusicTrack {

    private final GeometryClock.GridTime gridTime;
    protected final MusicClock musicClock;
    private final String rowName;

    private MultiValueMap<RelativeBeat, Movement<F>> trackMusic = DefaultCollections.multiMap();
    private MultiValueMap<RelativeBeat, FeatureTag<F>> trackTags = DefaultCollections.multiMap();

    public RhythmMusicTrack(String rowName, GeometryClock.GridTime gridTime, MusicClock musicClock) {
        this.rowName = rowName;
        this.gridTime = gridTime;
        this.musicClock = musicClock;
    }

    protected RelativeBeat getBeat(LocalDateTime moment) {
        return musicClock.getClosestBeat(gridTime.getRelativeTime(moment));
    }

    public void playEventAtBeat(LocalDateTime moment, F event) {
        trackMusic.add(getBeat(moment), new Movement<>(moment, event));
    }

    public F getLatestEventOnOrBeforeBeat(RelativeBeat beat) {
        F latestEvent = null;

        Iterator<RelativeBeat> iterator = musicClock.getBackwardsIterator(beat);

        while (iterator.hasNext()) {
            RelativeBeat prevBeat = iterator.next();
            List<Movement<F>> eventsAtBeat = trackMusic.get(prevBeat);
            if (eventsAtBeat != null && eventsAtBeat.size() > 0) {
                latestEvent = eventsAtBeat.get(eventsAtBeat.size() - 1).getFeature();
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
            List<Movement<F>> eventsAtBeat = trackMusic.get(beat);
            if (eventsAtBeat != null && eventsAtBeat.size() > 0) {
                for (Movement<F> movement : eventsAtBeat) {
                    features.add(movement.getFeature());
                }
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
        List<Movement<F>> featuresAtBeat = trackMusic.get(beat);
        if (featuresAtBeat != null && featuresAtBeat.size() > 0) {
            return featuresAtBeat.get(0).getFeature();
        }

        return null;
    }

    public F getLastFeatureAt(RelativeBeat beat) {
        List<Movement<F>> featuresAtBeat = trackMusic.get(beat);
        if (featuresAtBeat != null && featuresAtBeat.size() > 0) {
            return featuresAtBeat.get(featuresAtBeat.size() - 1).getFeature();
        }

        return null;
    }


    public List<F> getAllFeaturesAtBeat(RelativeBeat beat) {
        List<F> features = DefaultCollections.list();
        List<Movement<F>> eventsAtBeat = trackMusic.get(beat);
        if (eventsAtBeat != null && eventsAtBeat.size() > 0) {
            for (Movement<F> movement : eventsAtBeat) {
                features.add(movement.getFeature());
            }
        }

        return features;
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



    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    private static class Movement<F> {
        LocalDateTime position;
        F feature;
    }
}
