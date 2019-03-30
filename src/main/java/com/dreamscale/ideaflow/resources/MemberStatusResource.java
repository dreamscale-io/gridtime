package com.dreamscale.ideaflow.resources;

import com.dreamscale.ideaflow.api.ResourcePaths;
import com.dreamscale.ideaflow.api.organization.*;
import com.dreamscale.ideaflow.core.domain.OrganizationMemberEntity;
import com.dreamscale.ideaflow.core.security.RequestContext;
import com.dreamscale.ideaflow.core.service.MemberStatusService;
import com.dreamscale.ideaflow.core.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = ResourcePaths.STATUS_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class MemberStatusResource {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private MemberStatusService memberStatusService;

    /**
     * Get the active status of me
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.ME_PATH)
    public MemberWorkStatusDto getMyCurrentStatus() {
        RequestContext context = RequestContext.get();

        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(context.getMasterAccountId());

        return memberStatusService.getMyCurrentStatus(memberEntity.getOrganizationId(), memberEntity.getId());
    }


    /**
     * Get status of Me, and all my team members
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.TEAM_PATH)
    public List<MemberWorkStatusDto> getStatusOfMeAndMyTeam() {
        RequestContext context = RequestContext.get();

        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(context.getMasterAccountId());

        return memberStatusService.getStatusOfMeAndMyTeam(memberEntity.getOrganizationId(), memberEntity.getId());
    }


}
