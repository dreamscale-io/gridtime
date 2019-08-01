package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.output;

import com.dreamscale.htmflow.core.domain.tile.GridIdeaFlowMetricsEntity;
import com.dreamscale.htmflow.core.domain.tile.GridIdeaFlowMetricsRepository;
import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.analytics.query.IdeaFlowMetrics;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed.service.CalendarService;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.AggregateGrid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OutputIdeaFlow implements OutputStrategy {

    @Autowired
    CalendarService calendarService;

    @Autowired
    GridIdeaFlowMetricsRepository gridIdeaFlowMetricsRepository;

    @Override
    public void breatheOut(UUID torchieId, Metronome.Tick tick, AggregateGrid aggregateGrid) {

        IdeaFlowMetrics ideaFlowMetrics = IdeaFlowMetrics.queryFrom(aggregateGrid);

        Long tileSeq = calendarService.lookupTileSequenceNumber(tick.getFrom());

        GridIdeaFlowMetricsEntity metricsEntity = createEntity(torchieId, tileSeq, ideaFlowMetrics);
        gridIdeaFlowMetricsRepository.save(metricsEntity);

    }

    private GridIdeaFlowMetricsEntity createEntity(UUID torchieId, Long tileSeq, IdeaFlowMetrics ideaFlowMetrics) {
        GridIdeaFlowMetricsEntity entity = new GridIdeaFlowMetricsEntity();

        entity.setId(UUID.randomUUID());
        entity.setTorchieId(torchieId);
        entity.setZoomLevel(ideaFlowMetrics.getZoomLevel());
        entity.setTileSeq(tileSeq);
        entity.setAvgFlame(ideaFlowMetrics.getAvgFlame());
        entity.setPercentWtf(ideaFlowMetrics.getPercentWtf());
        entity.setPercentLearning(ideaFlowMetrics.getPercentLearning());
        entity.setPercentProgress(ideaFlowMetrics.getPercentProgress());
        entity.setPercentPairing(ideaFlowMetrics.getPercentPairing());
        return entity;
    }
}
