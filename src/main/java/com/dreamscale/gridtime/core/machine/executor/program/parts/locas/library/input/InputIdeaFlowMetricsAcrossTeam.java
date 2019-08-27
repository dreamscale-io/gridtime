package com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.input;

import com.dreamscale.gridtime.core.domain.tile.ZoomableTeamIdeaFlowMetricsEntity;
import com.dreamscale.gridtime.core.domain.tile.ZoomableTeamIdeaFlowMetricsRepository;
import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class InputIdeaFlowMetricsAcrossTeam implements InputStrategy<ZoomableTeamIdeaFlowMetricsEntity> {

    @Autowired
    ZoomableTeamIdeaFlowMetricsRepository zoomableTeamIdeaFlowMetricsRepository;

    @Autowired
    CalendarService calendarService;


    @Override
    public List<ZoomableTeamIdeaFlowMetricsEntity> breatheIn(UUID teamId, Metronome.Tick tick) {

        Long tileSeq = calendarService.lookupTileSequenceFromSameTime(tick.getZoomLevel(), tick.getFrom().getClockTime());

        //in this case, I need to query all the tiles for the team, that are at this particular grid time
        //there will be several at the same time from different team members

        return zoomableTeamIdeaFlowMetricsRepository.findByTeamIdAndZoomLevelAndTileSeq(teamId, tick.getZoomLevel(), tileSeq);

    }

}
