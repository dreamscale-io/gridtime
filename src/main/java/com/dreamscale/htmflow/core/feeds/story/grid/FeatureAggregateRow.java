package com.dreamscale.htmflow.core.feeds.story.grid;

import com.dreamscale.htmflow.core.feeds.story.music.MusicGeometryClock;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FeatureAggregateRow extends FeatureRow {

    private final FeatureAggregate aggregate;
    private final List<FeatureRow> sourceRows = new ArrayList<>();


    public FeatureAggregateRow(FeatureAggregate aggregate) {
        super(aggregate.getContainer());
        this.aggregate = aggregate;
    }

    public void addSourceRow(FeatureRow featureRow) {
        sourceRows.add(featureRow);
    }

    public void refreshMetrics() {
        resetAggregateMetrics();

        for (FeatureRow sourceRow: sourceRows) {
            Set<MusicGeometryClock.Coords> timingKeys = sourceRow.getTimingKeys();

            for (MusicGeometryClock.Coords timingKey : timingKeys) {
                GridMetrics sourceMetrics = sourceRow.findOrCreateMetrics(timingKey);
                GridMetrics aggregateMetrics = findOrCreateMetrics(timingKey);

                aggregateMetrics.combineWith(sourceMetrics);
            }
        }

    }

    private void resetAggregateMetrics() {
        Set<MusicGeometryClock.Coords> aggregateTimingKeys = getTimingKeys();
        for (MusicGeometryClock.Coords timingKey : aggregateTimingKeys) {
            findOrCreateMetrics(timingKey).resetMetrics();
        }
    }

}
