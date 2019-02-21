package com.dreamscale.htmflow.resources;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.batch.NewFlowBatch;
import com.dreamscale.htmflow.api.event.NewSnippetEvent;
import com.dreamscale.htmflow.api.organization.TeamMemberWorkStatusDto;
import com.dreamscale.htmflow.api.status.WtfStatusInputDto;
import com.dreamscale.htmflow.api.status.XPSummaryDto;
import com.dreamscale.htmflow.core.domain.OrganizationMemberEntity;
import com.dreamscale.htmflow.core.security.RequestContext;
import com.dreamscale.htmflow.core.service.FlowService;
import com.dreamscale.htmflow.core.service.OrganizationService;
import com.dreamscale.htmflow.core.service.WTFService;
import com.dreamscale.htmflow.core.service.XPService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.SPIRIT_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class SpiritResource {

    //TODO write a client for the new WTF status API

    //TODO write a test to make sure status is updated for the single user
    //TODO write a test to make sure status is updated for the team members
    //TODO status update for the team, should show up on open/close (refresh team)
    //TODO status update for hitting WTF button, should update immediately

    //TODO display flame on team member when WTF is pressed
    //TODO make WTF Flame by user clickable button to go to WTF view
    //TODO display WTF status problem statement information in the user's tooltip, in dark red
    //TODO sorting puts alarm status at the top

    //ticket 2 for ending WTF Session

    //TODO on inner view, create solved button that pops WTF status
    //TODO solved button updates active work status and clears alarm
    //TODO status icon goes from red to purple, and red text goes away in tooltip

    //ticket 3 add tooltips for finish/abort buttons

    //ticket 4 for timer (sync with server to account for idle time and laterz / resume)

    //TODO timer should update each minute, starting with duration in seconds, and display in minutes on team
    //TODO timer should update each second, starting with duration in seconds on WTF session page

    //ticket 5 show snippets

    //TODO retrieve the snippets from the server for display
    //TODO display snippets in the carousel

    //ticket 6 take SS coordination when click the carosel

    //ticket 7 show SS in carosel with snippets for local only

    //ticket 8 expand SS to full window

    //ticket 9, chat notes in window intermixed with snippets

    //ticket 10, send SS to server

    //ticket 11, click on WTF sessions of other users and see their scrapbook

    //ticket 12, click on WTF sessions and be able to participate in chat

    //ticket 13, get group XP when problem is solved

    @Autowired
    XPService xpService;

    @Autowired
    WTFService wtfService;

    @Autowired
    OrganizationService organizationService;

    /**
     * Gets the latest XP for the user's spirit
     * @return XPSummaryDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.XP_PATH)
    public XPSummaryDto getLatestXP() {
        RequestContext context = RequestContext.get();
        log.info("getLatestXP, user={}", context.getMasterAccountId());

        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(context.getMasterAccountId());

        return xpService.getLatestXPForMember(memberEntity.getId());
    }


    /**
     * Updates the status of the user's spirit to the current WTF situation
     * @param wtfStatusDto WtfStatusInputDto
     * @return TeamMemberWorkStatusDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.STATUS_PATH + ResourcePaths.WTF_PATH)
    public TeamMemberWorkStatusDto pushWTFStatus( @RequestBody WtfStatusInputDto wtfStatusDto) {
        RequestContext context = RequestContext.get();
        log.info("getLatestXP, user={}", context.getMasterAccountId());

        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(context.getMasterAccountId());

        return wtfService.pushWTFStatus(memberEntity.getOrganizationId(), memberEntity.getId(), wtfStatusDto.getProblemStatement());
    }

    /**
     * Updates the status of the user's spirit to resolve the current WTF situation with a yay
     * @return TeamMemberWorkStatusDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.STATUS_PATH + ResourcePaths.YAY_PATH)
    public TeamMemberWorkStatusDto resolveWithYay() {
        RequestContext context = RequestContext.get();
        log.info("getLatestXP, user={}", context.getMasterAccountId());

        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(context.getMasterAccountId());

        return wtfService.resolveWTFWithYay(memberEntity.getOrganizationId(), memberEntity.getId());
    }

    /**
     * Updates the status of the user's spirit to resolve the current WTF situation with an abort
     * @return TeamMemberWorkStatusDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.STATUS_PATH + ResourcePaths.ABORT_PATH)
    public TeamMemberWorkStatusDto resolveWithAbort() {
        RequestContext context = RequestContext.get();
        log.info("getLatestXP, user={}", context.getMasterAccountId());

        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(context.getMasterAccountId());

        return wtfService.resolveWTFWithAbort(memberEntity.getOrganizationId(), memberEntity.getId());
    }


}
