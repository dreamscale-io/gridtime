package com.dreamscale.gridtime.core.machine;

import com.dreamscale.gridtime.core.domain.tile.GridMarkerRepository;
import com.dreamscale.gridtime.core.domain.tile.GridRowRepository;
import com.dreamscale.gridtime.core.domain.tile.metrics.GridBoxMetricsRepository;
import com.dreamscale.gridtime.core.domain.tile.metrics.GridIdeaFlowMetricsRepository;
import com.dreamscale.gridtime.core.domain.work.TorchieFeedCursorRepository;
import com.dreamscale.gridtime.core.domain.work.WorkItemToAggregateRepository;
import com.dreamscale.gridtime.core.machine.Torchie;
import com.dreamscale.gridtime.core.machine.capabilities.cmd.TorchieCmd;
import com.dreamscale.gridtime.core.machine.executor.circuit.ProcessType;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import com.dreamscale.gridtime.core.machine.executor.dashboard.CircuitActivityDashboard;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService;
import com.dreamscale.gridtime.core.machine.executor.worker.PlexerWorkPile;
import com.dreamscale.gridtime.core.machine.executor.worker.SystemWorkPile;
import com.dreamscale.gridtime.core.machine.executor.worker.TorchieWorkPile;
import com.dreamscale.gridtime.core.machine.executor.worker.WorkPile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
public class FeedDataManager  {

    @Autowired
    CalendarService calendarService;

    @Autowired
    private TorchieFeedCursorRepository torchieFeedCursorRepository;

    @Autowired
    private WorkItemToAggregateRepository workItemToAggregateRepository;

    @Autowired
    private GridRowRepository gridRowRepository;

    @Autowired
    private GridMarkerRepository gridMarkerRepository;

    @Autowired
    private GridIdeaFlowMetricsRepository gridIdeaFlowMetricsRepository;

    @Autowired
    private GridBoxMetricsRepository gridBoxMetricsRepository;


    @Transactional
    public void purgeAll() {
        calendarService.purgeAll();

        purgeFeeds();
    }


    @Transactional
    public void purgeFeeds() {
        torchieFeedCursorRepository.truncate();
        workItemToAggregateRepository.truncate();

        gridRowRepository.truncate();
        gridMarkerRepository.truncate();
        gridIdeaFlowMetricsRepository.truncate();
        gridBoxMetricsRepository.truncate();
    }
}




