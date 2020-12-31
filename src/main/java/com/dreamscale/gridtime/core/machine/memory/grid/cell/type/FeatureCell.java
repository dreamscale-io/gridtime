package com.dreamscale.gridtime.core.machine.memory.grid.cell.type;

import com.dreamscale.gridtime.core.machine.clock.RelativeBeat;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.CellSize;
import com.dreamscale.gridtime.core.machine.memory.tag.FeatureTag;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeatureReference;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Getter
public class FeatureCell<F extends FeatureReference> implements GridCell {

    private int defaultCellSizeForBeat;
    private static final String EMPTY_CELL = "";

    private static final String TRUNCATED_INDICATOR = "*";


    private RelativeBeat beat;
    private F feature;
    private List<F> features;
    private List<FeatureTag<F>> tags;
    private List<String> featureGlyphs;

    public FeatureCell(RelativeBeat beat, F feature, List<FeatureTag<F>> tags) {
        this.beat = beat;
        this.feature = feature;
        this.tags = tags;

        this.defaultCellSizeForBeat = CellSize.calculateCellSize(beat);
    }

    public FeatureCell(RelativeBeat beat, List<F> features, List<FeatureTag<F>> tags) {
        this.beat = beat;
        this.features = features;
        this.tags = tags;

        this.defaultCellSizeForBeat = CellSize.calculateCellSize(beat);
    }

    public FeatureCell(RelativeBeat beat, List<F> features, List<String> featureGlyphs, List<FeatureTag<F>> tags) {
        this(beat, features, tags);
        this.featureGlyphs = featureGlyphs;
    }

    public String toHeaderCell() {
        return toHeaderCell(defaultCellSizeForBeat);
    }

    public String toHeaderCell(int overrideCellSize) {
        return toRightSizedCell(beat.toDisplayString(), "", overrideCellSize);
    }

    public String toValueCell() {
        return toValueCell(defaultCellSizeForBeat);
    }


    public String toValueCell(int overrideCellSize) {
        String featureStr = "";

        String tagStr = "";

        if (feature != null) {
            featureStr += feature.toDisplayString();
            if (tags != null && !tags.isEmpty()) {
                tagStr = toAllTagString();
            }
        } else if (features != null && !features.isEmpty()) {
            featureStr += toAllFeatureWithTagsString();
        } else {
            featureStr += EMPTY_CELL;
        }

        return toRightSizedCell(featureStr, tagStr, overrideCellSize);
    }

    @Override
    public Object toValue() {
        return toValueCell().trim();
    }

    @Override
    public List<UUID> getFeatureRefs() {
        List<UUID> refs = null;

        if (feature != null && (hasStartTag() || isFirstCell())) {
                refs = DefaultCollections.toList(feature.getFeatureId());
        } else if (features != null && !features.isEmpty()) {
            refs = DefaultCollections.list();
            for (FeatureReference feature : features) {
                refs.add(feature.getFeatureId());
            }
        }
        return refs;
    }

    public boolean hasStartTag() {
        if (tags != null && tags.size() > 0) {
            for (FeatureTag<F> tag : tags) {
                if (tag.isStart()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isFirstCell() {
        return beat.getBeat() == 1;
    }

    @Override
    public List<FeatureTag<?>> getFeatureTags() {
        List<FeatureTag<?>> featureTags = null;
        if (tags != null && tags.size() > 0) {
            featureTags = new ArrayList<>(tags);
        }
        return featureTags;
    }

    @Override
    public boolean hasFeature(FeatureReference reference) {
        if (feature != null && feature.equals(reference)) {
            return true;
        }

        if (features != null) {
            for (F featuredItem : features) {
                if (featuredItem.equals(reference)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public String toDisplayString() {
        String str = "";
        if (feature != null) {
            str += feature.toDisplayString();
            if (tags != null && !tags.isEmpty()) {
                str += toAllTagString();
            }
        } else if (features != null && !features.isEmpty()) {
            str += toAllFeatureWithTagsString();
        } else {
            str += EMPTY_CELL;
        }
        return str;
    }

    private String toAllFeatureWithTagsString() {
        String str = "";
        for (int i = 0; i < features.size(); i++) {
            F feature = features.get(i);
            String shorthand = getFeatureGlyph(i);

            if (shorthand != null) {
                str += shorthand;
            } else {
                str += feature.toDisplayString();
            }

            if (tags != null) {
                for (FeatureTag<F> featureTag : tags) {
                    if (featureTag.getFeature() == feature) {
                        str += featureTag.getTag().toDisplayString();
                    }
                }
            }
        }

        return str;
    }

    private String getFeatureGlyph(int i) {
        if (featureGlyphs != null) {
            return featureGlyphs.get(i);
        }
        return null;
    }

    @Override
    public List<String> getFeatureGlyphs() {
        return featureGlyphs;
    }

    private String toAllTagString() {
        String str = "";
        for (FeatureTag<F> tag : tags) {
            str += tag.getTag().toDisplayString();
        }

        return str;
    }

    public String toString() {
        return toValueCell();
    }

    private String toRightSizedCell(String featureStr, String tagStr, int cellSize) {
        String fittedContent;
        if ( isOverMaxSize(featureStr, tagStr, cellSize) ) {
            fittedContent = truncateFeature(featureStr, tagStr, cellSize);
        } else {
            fittedContent = StringUtils.rightPad(featureStr + tagStr, cellSize);
        }
        return fittedContent;
    }

    private String truncateFeature(String featureStr, String tagStr, int maxSize) {
        String truncatedTags = tagStr;
        if (tagStr.length() > maxSize - 1) {
            truncatedTags = tagStr.substring(0, maxSize - 1);
        }
        return featureStr.substring(0, maxSize - truncatedTags.length() - 1)  + TRUNCATED_INDICATOR + truncatedTags;
    }

    private boolean isOverMaxSize(String featureStr, String tagStr, int maxSize) {
        return featureStr.length() + tagStr.length() > maxSize;
    }

    private boolean isOverMaxSize(String contents, int maxSize) {
        return contents.length() > maxSize;
    }


}
