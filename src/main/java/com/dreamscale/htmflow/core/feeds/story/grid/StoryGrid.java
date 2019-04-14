package com.dreamscale.htmflow.core.feeds.story.grid;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.feature.context.Context;
import com.dreamscale.htmflow.core.feeds.story.feature.context.MomentOfContext;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.*;
import com.dreamscale.htmflow.core.feeds.story.music.MusicGeometryClock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StoryGrid {

    private final MusicGeometryClock clock;

    private Map<UUID, FeatureRow> featureRows = new HashMap<>();
    private Map<UUID, FeatureAggregateRow> aggregateRows = new HashMap<>();

    private Map<UUID, FeatureAggregate> aggregators = new HashMap<>();


    public StoryGrid(MusicGeometryClock clock) {
        this.clock = clock;
    }


    public GridMetrics getMetricsFor(FlowFeature feature) {
        return findOrCreateGridMetrics(feature, clock.getCoordinates());
    }

    public GridMetrics getMetricsFor(FlowFeature feature, MusicGeometryClock.Coords coords) {
        return findOrCreateGridMetrics(feature, coords);
    }

    private FeatureRow findOrCreateRow(FlowFeature feature) {
        FeatureRow row = featureRows.get(feature.getId());

        if (row == null) {
            row = new FeatureRow(feature);
            featureRows.put(feature.getId(), row);
        }

        return row;
    }


    private FeatureAggregate findOrCreateAggregate(FlowFeature feature) {
        FeatureAggregate aggregate = aggregators.get(feature.getId());

        if (aggregate == null) {
            aggregate = new FeatureAggregate(feature);
            aggregators.put(feature.getId(), aggregate);
        }

        return aggregate;
    }


    private GridMetrics findOrCreateGridMetrics(FlowFeature feature, MusicGeometryClock.Coords coords) {

        FeatureRow row = findOrCreateRow(feature);
        return row.findOrCreateMetrics(coords);
    }

    public void createAggregateRow(FlowFeature parent, List<? extends FlowFeature> children) {
        FeatureAggregate aggregate = findOrCreateAggregate(parent);
        aggregate.addItemsToAggregate(children);

        FeatureAggregateRow aggregateRow = new FeatureAggregateRow(aggregate);

        for (FlowFeature child : children) {
            aggregateRow.addSourceRow(featureRows.get(child.getId()));
        }

        aggregateRows.put(parent.getId(), aggregateRow);
    }

}
