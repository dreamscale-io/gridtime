package com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.input;

import com.dreamscale.gridtime.core.domain.tile.zoomable.ZoomableTeamIdeaFlowMetricsEntity;
import com.dreamscale.gridtime.core.domain.tile.zoomable.ZoomableTeamIdeaFlowMetricsRepository;
import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class InputIdeaFlowMetricsAcrossTeam implements AggregateInputStrategy<ZoomableTeamIdeaFlowMetricsEntity> {

    @Autowired
    ZoomableTeamIdeaFlowMetricsRepository zoomableTeamIdeaFlowMetricsRepository;

    @Autowired
    CalendarService calendarService;


    @Override
    public List<ZoomableTeamIdeaFlowMetricsEntity> breatheIn(UUID teamId, UUID torchieId, Metronome.TickScope tickScope) {

        Long tileSeq = calendarService.lookupTileSequenceFromSameTime(tickScope.getZoomLevel(), tickScope.getFrom().getClockTime());

        //in this case, I need to query all the tiles for the team, that are at this particular grid time
        //there will be several at the same time from different team members

        return zoomableTeamIdeaFlowMetricsRepository.findByTeamIdAndZoomLevelAndTileSeq(teamId, tickScope.getZoomLevel(), tileSeq);

    }

}
