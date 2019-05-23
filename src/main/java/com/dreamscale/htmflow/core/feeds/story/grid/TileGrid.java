package com.dreamscale.htmflow.core.feeds.story.grid;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.music.BeatSize;
import com.dreamscale.htmflow.core.feeds.story.music.MetronomePlayer;
import com.dreamscale.htmflow.core.feeds.story.music.clock.ClockBeat;
import com.dreamscale.htmflow.core.feeds.story.music.clock.MusicClock;

import java.time.LocalDateTime;
import java.util.*;

public class TileGrid {

    private Map<UUID, FeatureRow> featureRows = new HashMap<>();
    private Map<UUID, FeatureAggregateRow> aggregateRows = new HashMap<>();

    private Map<UUID, FeatureAggregate> aggregators = new HashMap<>();

    private List<Column> columns = new ArrayList<>();
    private final BeatSize gridBucketSize;

    private final MusicClock musicClock;

    private transient GridMetrics theVoid = new GridMetrics();
    private TileGridModel extractedTileGridModel;

    public TileGrid(MusicClock musicClock, BeatSize gridBucketSize) {
        this.gridBucketSize = gridBucketSize;
        this.musicClock = toClockSize(musicClock, gridBucketSize);
    }

    public MetronomePlayer createPlayer() {
        return new MetronomePlayer(musicClock);
    }

    public GridMetrics getMetricsFor(FlowFeature feature) {
        if (feature != null) {
            return findOrCreateGridMetrics(feature);
        } else {
            return theVoid;
        }
    }

    public GridMetrics getMetricsFor(FlowFeature feature, ClockBeat clockBeat) {
        if (feature != null) {
            return findOrCreateGridMetrics(feature, toBucketCoords(clockBeat));
        } else {
            return theVoid;
        }
    }


    public GridMetrics getAggregateMetricsFor(FlowFeature feature, ClockBeat clockBeat) {
        FeatureAggregateRow aggregateRow = aggregateRows.get(feature.getId());
        aggregateRow.refreshMetrics();

        return aggregateRow.findOrCreateMetrics(toBucketCoords(clockBeat));
    }

    private ClockBeat toBucketCoords(ClockBeat originalClockBeat) {
        if (originalClockBeat != null) {
            switch (gridBucketSize) {
                case BEAT:
                    return originalClockBeat;
                case QUARTER:
                    return originalClockBeat.toQuarter();
            }
        }
        return originalClockBeat;
    }

    private MusicClock toClockSize(MusicClock musicClock, BeatSize gridBucketSize) {
            switch (gridBucketSize) {
                case BEAT:
                    return musicClock;
                case QUARTER:
                    return musicClock.toQuartersClock();
            }

        return musicClock;
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

    private GridMetrics findOrCreateGridMetrics(FlowFeature feature, ClockBeat clockBeat) {

        FeatureRow row = findOrCreateRow(feature);
        return row.findOrCreateMetrics(clockBeat);
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


    public TileGridModel getStoryGridModel() {
        if (extractedTileGridModel == null) {
            extractedTileGridModel = createStoryGridModel();
        }

        return extractedTileGridModel;
    }

    /**
     * Note: This is dependent on URI Mappings already being run, if no URIs are present,
     * UUIDs will be used for URI
     */
    private TileGridModel createStoryGridModel() {

        TileGridModel tileGridModel = new TileGridModel();

        for (FeatureRow row : featureRows.values()) {
            FlowFeature feature = row.getFeature();
            GridMetrics metrics = row.getAllTimeBucket();


            tileGridModel.addMetricTotalsForFeature(feature, metrics);
        }

        for (FeatureAggregateRow aggregateRow : aggregateRows.values()) {
            aggregateRow.refreshMetrics();

            FlowFeature feature = aggregateRow.getFeature();
            GridMetrics metrics = aggregateRow.getAllTimeBucket();

            tileGridModel.addMetricTotalsForFeature(feature, metrics);
        }
        tileGridModel.setColumns(columns);



        return tileGridModel;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public List<FeatureRow> getRows() {
        ArrayList<FeatureRow> rows = new ArrayList<>(featureRows.values());

        rows.sort(new Comparator<FeatureRow>() {
            @Override
            public int compare(FeatureRow o1, FeatureRow o2) {
                return o1.getFeature().getRelativePath().compareTo(o2.getFeature().getRelativePath());
            }
        });

        return rows;

    }
}
