package com.dreamscale.htmflow.core.feeds.story.grid;

import com.dreamscale.htmflow.core.feeds.story.music.clock.ClockBeat;
import com.dreamscale.htmflow.core.feeds.story.music.clock.MusicClock;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
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
            Set<ClockBeat> timingKeys = sourceRow.getTimingKeys();

            for (ClockBeat timingKey : timingKeys) {
                GridMetrics sourceMetrics = sourceRow.findOrCreateMetrics(timingKey);
                GridMetrics aggregateMetrics = findOrCreateMetrics(timingKey);

                aggregateMetrics.combineWith(sourceMetrics);
            }
        }

    }


    private void resetAggregateMetrics() {
        Set<ClockBeat> aggregateTimingKeys = getTimingKeys();
        for (ClockBeat timingKey : aggregateTimingKeys) {
            findOrCreateMetrics(timingKey).resetMetrics();
        }
    }

}
