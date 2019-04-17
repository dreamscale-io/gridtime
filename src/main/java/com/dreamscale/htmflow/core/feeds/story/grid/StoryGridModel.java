package com.dreamscale.htmflow.core.feeds.story.grid;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.music.Snapshot;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StoryGridModel extends FlowFeature {

    private List<FeatureRow> featureRows;
    private List<FeatureAggregateRow> aggregateRows;

    private List<FeatureAggregate> aggregates;

    private List<Snapshot> snapshots;


}
