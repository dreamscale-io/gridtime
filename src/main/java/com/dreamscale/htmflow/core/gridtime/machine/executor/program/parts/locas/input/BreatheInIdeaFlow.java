package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.input;

import com.dreamscale.htmflow.core.domain.tile.GridIdeaFlowMetricsEntity;
import com.dreamscale.htmflow.core.domain.tile.GridIdeaFlowMetricsRepository;
import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.machine.clock.ZoomLevel;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.analytics.query.IdeaFlowMetrics;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class BreatheInIdeaFlow implements BreatheInStrategy<IdeaFlowMetrics> {

    @Autowired
    GridIdeaFlowMetricsRepository gridIdeaFlowMetricsRepository;

    @Autowired
    CalendarService calendarService;


    @Override
    public List<IdeaFlowMetrics> breatheIn(UUID torchieId, Metronome.Tick zoomedOutTick) {

        //suck in child tiles, within the range of this one

        ZoomLevel baseZoom = zoomedOutTick.getZoomLevel();
        ZoomLevel zoomInOneLevel = zoomedOutTick.getZoomLevel().zoomIn();

        Long zoomInSequenceStart = calendarService.lookupTileSequenceFromSameTime(zoomInOneLevel, zoomedOutTick.getFrom().getClockTime());
        Long zoomInSequenceEnd = zoomInSequenceStart + baseZoom.getInnerBeats() - 1;

        List<GridIdeaFlowMetricsEntity> selectedMetrics = gridIdeaFlowMetricsRepository.findByTorchieZoomRange(
                torchieId,
                zoomInOneLevel.name(),
                zoomInSequenceStart,
                zoomInSequenceEnd);

        return toIdeaFlowMetrics(selectedMetrics);
    }

    private List<IdeaFlowMetrics> toIdeaFlowMetrics(List<GridIdeaFlowMetricsEntity> selectedMetrics) {
        List<IdeaFlowMetrics> ideaFlowMetrics = new ArrayList<>();

        for (GridIdeaFlowMetricsEntity metricsEntity : selectedMetrics) {
            IdeaFlowMetrics metrics = new IdeaFlowMetrics(metricsEntity.getZoomLevel(),
                    Duration.ofSeconds(metricsEntity.getTimeInTile()),
                    metricsEntity.getAvgFlame(),
                    metricsEntity.getPercentWtf(),
                    metricsEntity.getPercentLearning(),
                    metricsEntity.getPercentProgress(),
                    metricsEntity.getPercentPairing());

            ideaFlowMetrics.add(metrics);
        }

        return ideaFlowMetrics;
    }
}
