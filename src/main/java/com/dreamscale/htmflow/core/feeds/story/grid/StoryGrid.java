package com.dreamscale.htmflow.core.feeds.story.grid;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.music.BeatSize;
import com.dreamscale.htmflow.core.feeds.story.music.MetronomePlayer;
import com.dreamscale.htmflow.core.feeds.story.music.MusicClock;

import java.time.LocalDateTime;
import java.util.*;

public class StoryGrid {


    private Map<UUID, FeatureRow> featureRows = new HashMap<>();
    private Map<UUID, FeatureAggregateRow> aggregateRows = new HashMap<>();

    private Map<UUID, FeatureAggregate> aggregators = new HashMap<>();

    private List<Column> columns = new ArrayList<>();
    private final BeatSize timeBucketSize;
    private final LocalDateTime from;
    private final LocalDateTime to;

    private transient GridMetrics theVoid = new GridMetrics();
    private StoryGridModel extractedStoryGridModel;

    public StoryGrid(BeatSize timeBucketSize, LocalDateTime from, LocalDateTime to) {
        this.timeBucketSize = timeBucketSize;
        this.from = from;
        this.to = to;
    }

    public MetronomePlayer createPlayer() {
        MusicClock clock = new MusicClock(from, to);
        return new MetronomePlayer(clock, timeBucketSize);
    }

    public GridMetrics getMetricsFor(FlowFeature feature) {
        if (feature != null) {
            return findOrCreateGridMetrics(feature);
        } else {
            return theVoid;
        }
    }

    public GridMetrics getMetricsFor(FlowFeature feature, MusicClock.Beat beat) {
        if (feature != null) {
            return findOrCreateGridMetrics(feature, toBucketCoords(beat));
        } else {
            return theVoid;
        }
    }


    public GridMetrics getAggregateMetricsFor(FlowFeature feature, MusicClock.Beat beat) {
        FeatureAggregateRow aggregateRow = aggregateRows.get(feature.getId());
        aggregateRow.refreshMetrics();

        return aggregateRow.findOrCreateMetrics(toBucketCoords(beat));
    }

    private MusicClock.Beat toBucketCoords(MusicClock.Beat originalBeat) {
        switch (timeBucketSize) {
            case BEAT:
                return originalBeat;
            case QUARTER:
                return originalBeat.toQuarter();
            case HALF:
                return originalBeat.toHalf();
        }
        return originalBeat;
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

    private GridMetrics findOrCreateGridMetrics(FlowFeature feature, MusicClock.Beat beat) {

        FeatureRow row = findOrCreateRow(feature);
        return row.findOrCreateMetrics(beat);
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


    public StoryGridModel getStoryGridModel() {
        if (extractedStoryGridModel == null) {
            extractedStoryGridModel = createStoryGridModel();
        }

        return extractedStoryGridModel;
    }

    /**
     * Note: This is dependent on URI Mappings already being run, if no URIs are present,
     * UUIDs will be used for URI
     */
    private StoryGridModel createStoryGridModel() {

        StoryGridModel storyGridModel = new StoryGridModel();

        for (FeatureRow row : featureRows.values()) {
            FlowFeature feature = row.getFeature();
            GridMetrics metrics = row.getAllTimeBucket();


            storyGridModel.addMetricTotalsForFeature(feature, metrics);
        }

        for (FeatureAggregateRow aggregateRow : aggregateRows.values()) {
            aggregateRow.refreshMetrics();

            FlowFeature feature = aggregateRow.getFeature();
            GridMetrics metrics = aggregateRow.getAllTimeBucket();

            storyGridModel.addMetricTotalsForFeature(feature, metrics);
        }
        storyGridModel.setColumns(columns);



        return storyGridModel;
    }

}
