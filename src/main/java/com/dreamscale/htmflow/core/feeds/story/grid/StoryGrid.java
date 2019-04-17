package com.dreamscale.htmflow.core.feeds.story.grid;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.music.MusicGeometryClock;
import com.dreamscale.htmflow.core.feeds.story.music.Snapshot;

import java.util.*;

public class StoryGrid {

    private Map<UUID, FeatureRow> featureRows = new HashMap<>();
    private Map<UUID, FeatureAggregateRow> aggregateRows = new HashMap<>();

    private Map<UUID, FeatureAggregate> aggregators = new HashMap<>();

    private List<Snapshot> snapshots = new ArrayList<>();

    public StoryGrid() {
    }

    public StoryGrid(StoryGridModel storyGridModel) {
        for (FeatureRow featureRow : storyGridModel.getFeatureRows()) {
            featureRows.put(featureRow.getFeature().getId(), featureRow);
        }

        for (FeatureAggregateRow aggregateRow : storyGridModel.getAggregateRows()) {
            aggregateRows.put(aggregateRow.getAggregate().getId(), aggregateRow);
        }

        for (FeatureAggregate aggregate : storyGridModel.getAggregates()) {
            aggregators.put(aggregate.getId(), aggregate);
        }

        snapshots.addAll(storyGridModel.getSnapshots());
    }


    public GridMetrics getMetricsFor(FlowFeature feature) {
        return findOrCreateGridMetrics(feature);
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

    private GridMetrics findOrCreateGridMetrics(FlowFeature feature) {

        FeatureRow row = findOrCreateRow(feature);
        return row.findOrCreateMetrics();
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

    public void addSnapshot(Snapshot snapshot) {
        snapshot.setRelativeSequence(snapshots.size() + 1);
        snapshots.add(snapshot);
    }

    public StoryGridModel extractStoryGridModel() {
        for (FeatureAggregateRow aggregateRow : aggregateRows.values()) {
            aggregateRow.refreshMetrics();
        }

        StoryGridModel storyGridModel = new StoryGridModel();
        storyGridModel.setFeatureRows(new ArrayList<>(featureRows.values()));
        storyGridModel.setAggregateRows(new ArrayList<>(aggregateRows.values()));
        storyGridModel.setAggregates(new ArrayList<>(aggregators.values()));
        storyGridModel.setSnapshots(snapshots);
        return storyGridModel;
    }
}
