package com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.input;

import com.dreamscale.gridtime.core.domain.tile.zoomable.ZoomableBoxMetricsEntity;
import com.dreamscale.gridtime.core.domain.tile.zoomable.ZoomableBoxMetricsRepository;
import com.dreamscale.gridtime.core.domain.tile.zoomable.ZoomableIdeaFlowMetricsEntity;
import com.dreamscale.gridtime.core.domain.tile.zoomable.ZoomableIdeaFlowMetricsRepository;
import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class InputBoxMetrics implements InputStrategy<ZoomableBoxMetricsEntity> {

    @Autowired
    ZoomableBoxMetricsRepository zoomableBoxMetricsRepository;

    @Autowired
    CalendarService calendarService;


    @Override
    public List<ZoomableBoxMetricsEntity> breatheIn(UUID torchieId, Metronome.Tick zoomedOutTick) {

        //suck in child tiles, within the range of this one

        ZoomLevel baseZoom = zoomedOutTick.getZoomLevel();
        ZoomLevel zoomInOneLevel = zoomedOutTick.getZoomLevel().zoomIn();

        Long zoomInSequenceStart = calendarService.lookupTileSequenceFromSameTime(zoomInOneLevel, zoomedOutTick.getFrom().getClockTime());

        Long zoomInSequenceEnd = zoomInSequenceStart + baseZoom.getInnerBeats() - 1;

        return zoomableBoxMetricsRepository.findByTorchieZoomRange(
                torchieId,
                zoomInOneLevel.name(),
                zoomInSequenceStart,
                zoomInSequenceEnd);
    }

}
