package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.core.service.FlowService;
import com.dreamscale.gridtime.core.service.OrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.TORCHIE_PATH + ResourcePaths.JOB_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class TorchieJobResource {

    @Autowired
    FlowService flowService;


    @Autowired
    OrganizationService organizationService;


    //start torchies for team

    // /torchie/job/team/{id}/transition/start 1:11 11:11

//    @PreAuthorize("hasRole('ROLE_USER')")
//    @PostMapping(ResourcePaths.TEAM_PATH + "/{id}" + ResourcePaths.TRANSITION_PATH + ResourcePaths.START_PATH)
//    public TorchieJobStatus startTeamTorchieFeed(@PathVariable("id") String teamIdStr) {
//
//        UUID teamId = UUID.fromString(teamIdStr);
//
//        return torchieExecutorService.startTeamTorchie(teamId);
//    }
//
//    // /torchie/job/member/{id}/transition/start 1:11 11:11
//
//    @PreAuthorize("hasRole('ROLE_USER')")
//    @PostMapping(ResourcePaths.MEMBER_PATH + "/{id}"+ ResourcePaths.TRANSITION_PATH + ResourcePaths.START_PATH)
//    public CircuitMonitor startMemberTorchieFeed(@PathVariable("id") String memberIdStr) {
//
//        RequestContext context = RequestContext.get();
//
//        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(context.getMasterAccountId());
//
//        UUID memberId = UUID.fromString(memberIdStr);
//        organizationService.validateMemberWithinOrgByMemberId(memberEntity.getOrganizationId(), memberId);
//
//        return torchieService.startMemberTorchie(memberId);
//
//    }



}
