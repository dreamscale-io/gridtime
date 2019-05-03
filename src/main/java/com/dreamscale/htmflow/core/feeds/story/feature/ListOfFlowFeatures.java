package com.dreamscale.htmflow.core.feeds.story.feature;

import com.dreamscale.htmflow.core.domain.tile.FlowObjectType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ListOfFlowFeatures extends FlowFeature {
    private List<? extends FlowFeature> featureList;

    public ListOfFlowFeatures(List<? extends FlowFeature> featureList) {
        this();
        this.featureList = featureList;
        setFlowObjectType(FlowObjectType.LIST_OF_FEATURES);
    }

    public ListOfFlowFeatures() {
        super(FlowObjectType.LIST_OF_FEATURES);
    }

    @JsonIgnore
    public List<? extends FlowFeature> getOriginalTypedList() {
        return featureList;
    }
}
