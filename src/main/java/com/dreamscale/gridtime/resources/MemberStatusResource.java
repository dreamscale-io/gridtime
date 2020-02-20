package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.organization.*;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.capability.active.MemberStatusCapability;
import com.dreamscale.gridtime.core.capability.directory.OrganizationMembershipCapability;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = ResourcePaths.STATUS_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class MemberStatusResource {

    @Autowired
    private OrganizationMembershipCapability organizationMembership;

    @Autowired
    private MemberStatusCapability memberStatusCapability;

    /**
     * Get the active status of me
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.ME_PATH)
    public MemberWorkStatusDto getMyCurrentStatus() {
        RequestContext context = RequestContext.get();

        OrganizationMemberEntity memberEntity = organizationMembership.getDefaultMembership(context.getRootAccountId());

        return memberStatusCapability.getMyCurrentStatus(memberEntity.getOrganizationId(), memberEntity.getId());
    }


    /**
     * Get status of Me, and all my team members
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.TEAM_PATH)
    public List<MemberWorkStatusDto> getStatusOfMeAndMyTeam() {
        RequestContext context = RequestContext.get();

        OrganizationMemberEntity memberEntity = organizationMembership.getDefaultMembership(context.getRootAccountId());

        return memberStatusCapability.getStatusOfMeAndMyTeam(memberEntity.getOrganizationId(), memberEntity.getId());
    }



    // [ GT ]

}
