package com.dreamscale.htmflow.core.feeds.story.grid;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;

import java.util.ArrayList;
import java.util.List;

public class FeatureAggregate extends FlowFeature {

    private final FlowFeature container;

    List<? extends FlowFeature> itemsToAggregate = new ArrayList<>();

    public FeatureAggregate(FlowFeature container) {
        this.container = container;
    }

    public void addItemsToAggregate(List<? extends FlowFeature> itemsToAggregate) {
        this.itemsToAggregate = itemsToAggregate;
    }

    public FlowFeature getContainer() {
        return container;
    }
}
