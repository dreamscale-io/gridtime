package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.job.JobStatusDto;
import com.dreamscale.gridtime.api.job.SystemJobStatusDto;
import com.dreamscale.gridtime.api.job.WatchConfigurationDto;
import com.dreamscale.gridtime.core.capability.directory.OrganizationMembershipCapability;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.capability.operator.GridtimeJobManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.JOB_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class JobResource {

    @Autowired
    private GridtimeJobManager gridtimeJobManager;

    @Autowired
    private OrganizationMembershipCapability organizationMembership;


    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.SYSTEM_PATH )
    List<SystemJobStatusDto> getSystemJobs() {
        RequestContext context = RequestContext.get();
        log.info("getSystemJobs, user={}", context.getRootAccountId());

        return gridtimeJobManager.getAllSystemJobs(context.getRootAccountId());
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH  )
    List<JobStatusDto> getTeamJobs() {
        RequestContext context = RequestContext.get();
        log.info("getOrganizationJobs, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationMembership.getDefaultMembership(context.getRootAccountId());

        return gridtimeJobManager.getAllJobsForTeam(invokingMember.getOrganizationId(), invokingMember.getId());
    }


    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.ORGANIZATION_PATH  )
    List<JobStatusDto> getOrganizationJobs() {
        RequestContext context = RequestContext.get();
        log.info("getOrganizationJobs, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationMembership.getDefaultMembership(context.getRootAccountId());

        return gridtimeJobManager.getAllJobsForOrganization(invokingMember.getOrganizationId());
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{jobId}" + ResourcePaths.START_PATH )
    JobStatusDto startJob(@PathVariable("jobId") String jobId) {
        RequestContext context = RequestContext.get();
        log.info("startJob, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationMembership.getDefaultMembership(context.getRootAccountId());

        return gridtimeJobManager.startJob(invokingMember.getOrganizationId(), invokingMember.getId(), jobId);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{jobId}" + ResourcePaths.STOP_PATH )
    JobStatusDto stopJob(@PathVariable("jobId") String jobId) {
        RequestContext context = RequestContext.get();
        log.info("stopJob, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationMembership.getDefaultMembership(context.getRootAccountId());

        return gridtimeJobManager.stopJob(invokingMember.getOrganizationId(), invokingMember.getId(), jobId);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{jobId}" + ResourcePaths.WATCH_PATH )
    JobStatusDto watchJob(@PathVariable("jobId") String jobId, @RequestBody WatchConfigurationDto watchConfigurationDto) {
        RequestContext context = RequestContext.get();
        log.info("watchJob, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationMembership.getDefaultMembership(context.getRootAccountId());

        return gridtimeJobManager.watchJob(invokingMember.getOrganizationId(), invokingMember.getId(), jobId, watchConfigurationDto);
    }


    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{jobId}"  )
    JobStatusDto getJobStatus(@PathVariable("jobId") String jobId) {
        RequestContext context = RequestContext.get();
        log.info("getJobStatus, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationMembership.getDefaultMembership(context.getRootAccountId());

        return gridtimeJobManager.getJobStatus(invokingMember.getOrganizationId(), invokingMember.getId(), jobId);
    }

    //so what I want to do, is be able to have a list of descriptive jobs

    //and be able to submit those jobs to Grid

    //then Grid manages a set of active jobs it's supposed to be running.

    //vision of it's template of what it ought to be doing.

    //GridTime Job Configuration Template

    //when a gridtime instance loads, it loads up it's job configuration.

    //you can get the job configuration, change the configuration, get the current status of the running jobs

    //tell a particular job to run.


}
