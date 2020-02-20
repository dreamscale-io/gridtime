package com.dreamscale.gridtime.core.service;

import com.dreamscale.gridtime.api.job.JobStatusDto;
import com.dreamscale.gridtime.api.job.SystemJobStatusDto;
import com.dreamscale.gridtime.core.machine.GridTimeEngine;
import com.dreamscale.gridtime.core.machine.Torchie;
import com.dreamscale.gridtime.core.machine.TorchieFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class GridtimeJobManager {

    @Autowired
    GridTimeEngine gridTimeEngine;

    @Autowired
    TimeService timeService;

    @Autowired
    TorchieFactory torchieFactory;

    public void generateCalendar() {

        LocalDateTime calendarUntil = timeService.now().plusDays(90);

        Torchie torchie = torchieFactory.wireUpCalendarTorchieToRunUntil(calendarUntil);
        gridTimeEngine.submitJob(torchie);

    }
    

    /*

    Okay so I've got a bunch of teams configured in the DB, and a bunch of torchie's configured in the DB
    And all of these have to be running on some server somewhere in the cluster.

    Similar to the way we lock for work in picking up aggregate work that needs doing on demand,
    If I've got a max capacity of X on this particular server, then I need to be able to pick up X jobs,
    and leave another server to pick up the rest of them.

    Then if this server suddenly dies, or is rebooted, it needs to keep an updated last active thing,
    So that if anything "dies" it will get picked up in the pool again.

    If I say I want to start my Torchie process, then it should get forever picked up and started.
    Even as the various services start/stop.

    If I haven't generated history, I need a special historical generator for the past things.

     */


    public List<SystemJobStatusDto> getAllSystemJobs(UUID rootAccountId) {
        return null;
    }

    public List<JobStatusDto> getAllJobsForOrganization(UUID organizationId) {
        return null;
    }

    public List<JobStatusDto> getAllJobsForTeam(UUID organizationId, UUID memberId) {
        return null;
    }

    public JobStatusDto startJob(UUID organizationId, UUID memberId, String jobId) {
        return null;
    }

    public JobStatusDto stopJob(UUID organizationId, UUID memberId, String jobId) {
        return null;
    }

    public JobStatusDto getJobStatus(UUID organizationId, UUID memberId, String jobId) {
        return null;
    }
}
