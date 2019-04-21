package com.dreamscale.htmflow.core.feeds.story.grid;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.music.MusicGeometryClock;
import com.dreamscale.htmflow.core.feeds.story.music.Column;

import java.util.*;

public class StoryGrid {

    private Map<UUID, FeatureRow> featureRows = new HashMap<>();
    private Map<UUID, FeatureAggregateRow> aggregateRows = new HashMap<>();

    private Map<UUID, FeatureAggregate> aggregators = new HashMap<>();

    private transient GridMetrics theVoid = new GridMetrics();

    private List<Column> columns = new ArrayList<>();



    public GridMetrics getMetricsFor(FlowFeature feature) {
        if (feature != null) {
            return findOrCreateGridMetrics(feature);
        } else {
            return theVoid;
        }
    }

    public GridMetrics getMetricsFor(FlowFeature feature, MusicGeometryClock.Coords coords) {
        if (feature != null) {
            return findOrCreateGridMetrics(feature, coords);
        } else {
            return theVoid;
        }
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
        return row.getAllTimeBucket();
    }

    private GridMetrics findOrCreateGridMetrics(FlowFeature feature, MusicGeometryClock.Coords coords) {

        FeatureRow row = findOrCreateRow(feature);
        return row.findOrCreateMetrics(coords);
    }

    public GridMetrics getAggregateMetricsFor(FlowFeature feature, MusicGeometryClock.Coords coords) {
        FeatureAggregateRow aggregateRow = aggregateRows.get(feature.getId());
        aggregateRow.refreshMetrics();

        return aggregateRow.findOrCreateMetrics(coords);
    }

    public void createAggregateRow(FlowFeature parent, List<? extends FlowFeature> children) {
        FeatureAggregate aggregate = findOrCreateAggregate(parent);
        aggregate.addItemsToAggregate(children);

        FeatureAggregateRow aggregateRow = new FeatureAggregateRow(aggregate);

        for (FlowFeature child : children) {
            FeatureRow row = featureRows.get(child.getId());
            if (row != null) {
                aggregateRow.addSourceRow(row);
            }
        }

        aggregateRows.put(parent.getId(), aggregateRow);
    }

    public void addColumn(Column column) {
        column.setRelativeSequence(columns.size() + 1);
        columns.add(column);
    }

    /**
     * Note: This is dependent on URI Mappings already being run, if no URIs are present,
     * UUIDs will be used for URI
     * @return
     */
    public StoryGridModel extractStoryGridModel() {
        StoryGridModel storyGridModel = new StoryGridModel();

        for (FeatureRow row : featureRows.values()) {
            FlowFeature feature = row.getFeature();
            GridMetrics metrics = row.getAllTimeBucket();

            storyGridModel.addActivityForStructure(feature, metrics);
        }

        for (FeatureAggregateRow aggregateRow : aggregateRows.values()) {
            aggregateRow.refreshMetrics();

            FlowFeature feature = aggregateRow.getFeature();
            GridMetrics metrics = aggregateRow.getAllTimeBucket();

            storyGridModel.addActivityForStructure(feature, metrics);
        }

        storyGridModel.setColumns(columns);
        return storyGridModel;
    }

}
