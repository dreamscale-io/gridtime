package com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.input;

import com.dreamscale.gridtime.core.domain.tile.zoomable.ZoomableTeamBoxMetricsEntity;
import com.dreamscale.gridtime.core.domain.tile.zoomable.ZoomableTeamBoxMetricsRepository;
import com.dreamscale.gridtime.core.domain.tile.zoomable.ZoomableTeamIdeaFlowMetricsEntity;
import com.dreamscale.gridtime.core.domain.tile.zoomable.ZoomableTeamIdeaFlowMetricsRepository;
import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class InputBoxMetricsAcrossTeam implements InputStrategy<ZoomableTeamBoxMetricsEntity> {

    @Autowired
    ZoomableTeamBoxMetricsRepository zoomableTeamBoxMetricsRepository;

    @Autowired
    CalendarService calendarService;


    @Override
    public List<ZoomableTeamBoxMetricsEntity> breatheIn(UUID teamId, Metronome.Tick tick) {

        Long tileSeq = calendarService.lookupTileSequenceFromSameTime(tick.getZoomLevel(), tick.getFrom().getClockTime());

        //in this case, I need to query all the tiles for the team, that are at this particular grid time
        //there will be several at the same time from different team members

        return zoomableTeamBoxMetricsRepository.findByTeamIdAndZoomLevelAndTileSeq(teamId, tick.getZoomLevel(), tileSeq);

    }

}
