package com.dreamscale.gridtime.core.machine.executor.program.parts.transform;

import com.dreamscale.gridtime.core.domain.tile.GridMarkerRepository;
import com.dreamscale.gridtime.core.domain.tile.GridRowRepository;
import com.dreamscale.gridtime.core.domain.tile.metrics.GridBoxMetricsRepository;
import com.dreamscale.gridtime.core.domain.tile.metrics.GridIdeaFlowMetricsRepository;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService;
import com.dreamscale.gridtime.core.machine.memory.TorchieState;
import com.dreamscale.gridtime.core.machine.memory.tile.GridTile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class DeleteOldTileTransform implements TransformStrategy {

    @Autowired
    GridRowRepository gridRowRepository;

    @Autowired
    GridMarkerRepository gridMarkerRepository;

    @Autowired
    GridIdeaFlowMetricsRepository gridIdeaFlowMetricsRepository;

    @Autowired
    GridBoxMetricsRepository gridBoxMetricsRepository;

    @Autowired
    CalendarService calendarService;

    @Override
    public void transform(TorchieState torchieState) {

        GridTile gridTile = torchieState.getActiveTile();
        UUID calendarId = calendarService.lookupCalendarId(gridTile.getGridTime());

        gridRowRepository.deleteByTorchieIdAndCalendarId(torchieState.getTorchieId(), calendarId);
        gridMarkerRepository.deleteByTorchieIdAndCalendarId(torchieState.getTorchieId(), calendarId);
        gridIdeaFlowMetricsRepository.deleteByTorchieIdAndCalendarId(torchieState.getTorchieId(), calendarId);
        gridBoxMetricsRepository.deleteByTorchieIdAndCalendarId(torchieState.getTorchieId(), calendarId);
    }

}
