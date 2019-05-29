package com.dreamscale.htmflow.core.gridtime.executor.memory.grid.cell;

import com.dreamscale.htmflow.core.gridtime.executor.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.FeatureTag;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.reference.FeatureReference;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Getter
public class FeatureCell<F extends FeatureReference> implements GridCell {
    private static final int DEFAULT_CELL_SIZE = 8;
    private static final String EMPTY_CELL = "  ";

    private static final String TRUNCATED_INDICATOR = "*";

    private RelativeBeat beat;
    private F feature;
    private List<F> features;
    private List<FeatureTag<F>> tags;
    private List<String> featureShorthands;

    public FeatureCell(RelativeBeat beat, F feature, List<FeatureTag<F>> tags) {
        this.beat = beat;
        this.feature = feature;
        this.tags = tags;
    }

    public FeatureCell(RelativeBeat beat, List<F> features, List<FeatureTag<F>> tags) {
        this.beat = beat;
        this.features = features;
        this.tags = tags;
    }

    public FeatureCell(RelativeBeat beat, List<F> features, List<String> featureShorthands, List<FeatureTag<F>> tags) {
        this.beat = beat;
        this.features = features;
        this.featureShorthands = featureShorthands;
        this.tags = tags;
    }

    public String getHeaderCell() {
        return getHeaderCell(DEFAULT_CELL_SIZE);
    }

    public String getHeaderCell(int overrideCellSize) {
        return toRightSizedCell("|"+beat.toShortString(), "", overrideCellSize);
    }

    public String getValueCell() {
        return getValueCell(DEFAULT_CELL_SIZE);
    }

    public String getValueCell(int overrideCellSize) {
        String featureStr = "|";

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

    private String toAllFeatureWithTagsString() {
        String str = "";
        for (int i = 0; i < features.size(); i++) {
            F feature = features.get(i);
            String shorthand = getFeatureShorthand(i);

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

    private String getFeatureShorthand(int i) {
        if (featureShorthands != null) {
            return featureShorthands.get(i);
        }
        return null;
    }

    private String toAllTagString() {
        String str = "";
        for (FeatureTag<F> tag : tags) {
            str += tag.getTag().toDisplayString();
        }

        return str;
    }

    public String toString() {
        return getValueCell();
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
        return featureStr.substring(0, maxSize - tagStr.length() - 1)  + TRUNCATED_INDICATOR + tagStr;
    }

    private boolean isOverMaxSize(String featureStr, String tagStr, int maxSize) {
        return featureStr.length() + tagStr.length() > maxSize;
    }

    private boolean isOverMaxSize(String contents, int maxSize) {
        return contents.length() > maxSize;
    }

}
