package com.dreamscale.htmflow.resources;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.torchie.TorchieJobStatus;
import com.dreamscale.htmflow.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.htmflow.core.security.RequestContext;
import com.dreamscale.htmflow.core.service.FlowService;
import com.dreamscale.htmflow.core.service.OrganizationService;
import com.dreamscale.htmflow.core.service.TorchieExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.TORCHIE_PATH + ResourcePaths.JOB_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class TorchieJobResource {

    @Autowired
    FlowService flowService;

    @Autowired
    TorchieExecutorService torchieExecutorService;

    @Autowired
    OrganizationService organizationService;


    //start torchies for team

    // /torchie/job/team/{id}/transition/start 1:11 11:11


    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.TEAM_PATH + "/{id}" + ResourcePaths.TRANSITION_PATH + ResourcePaths.START_PATH)
    public TorchieJobStatus startTeamTorchieFeed(@PathVariable("id") String teamIdStr) {

        UUID teamId = UUID.fromString(teamIdStr);

        return torchieExecutorService.startTeamTorchie(teamId);
    }

    // /torchie/job/member/{id}/transition/start 1:11 11:11

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.MEMBER_PATH + "/{id}"+ ResourcePaths.TRANSITION_PATH + ResourcePaths.START_PATH)
    public TorchieJobStatus startMemberTorchieFeed(@PathVariable("id") String memberIdStr) {

        RequestContext context = RequestContext.get();

        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(context.getMasterAccountId());

        UUID memberId = UUID.fromString(memberIdStr);
        organizationService.validateMemberWithinOrgByMemberId(memberEntity.getOrganizationId(), memberId);

        return torchieExecutorService.startMemberTorchie(memberId);

    }



}
