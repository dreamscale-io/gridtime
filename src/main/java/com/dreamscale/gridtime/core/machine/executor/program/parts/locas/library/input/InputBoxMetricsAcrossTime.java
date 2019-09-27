package com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.input;

import com.dreamscale.gridtime.core.domain.tile.zoomable.ZoomableBoxMetricsEntity;
import com.dreamscale.gridtime.core.domain.tile.zoomable.ZoomableBoxMetricsRepository;
import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureResolverService;
import com.dreamscale.gridtime.core.machine.memory.feature.details.Box;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.PlaceReference;
import com.dreamscale.gridtime.core.machine.memory.grid.query.metrics.BoxMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class InputBoxMetricsAcrossTime implements InputStrategy<ZoomableBoxMetricsEntity> {

    @Autowired
    ZoomableBoxMetricsRepository zoomableBoxMetricsRepository;

    @Autowired
    CalendarService calendarService;

    @Autowired
    FeatureResolverService featureResolverService;


    @Override
    public List<ZoomableBoxMetricsEntity> breatheIn(UUID teamId, UUID torchieId, Metronome.TickScope tickScope) {

        //suck in child tiles, within the range of this one
        //so this is going to give me all box metrics, with

        ZoomLevel baseZoom = tickScope.getZoomLevel();
        ZoomLevel zoomInOneLevel = tickScope.getZoomLevel().zoomIn();

        Long zoomInSequenceStart = calendarService.lookupTileSequenceFromSameTime(zoomInOneLevel, tickScope.getFrom().getClockTime());

        Long zoomInSequenceEnd = zoomInSequenceStart + baseZoom.getInnerBeats() - 1;

        return zoomableBoxMetricsRepository.findByTorchieZoomRange(
                torchieId,
                zoomInOneLevel.name(),
                zoomInSequenceStart,
                zoomInSequenceEnd);
    }


}
