package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.organization.*;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.capability.active.MemberCapability;
import com.dreamscale.gridtime.core.capability.directory.OrganizationCapability;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = ResourcePaths.MEMBER_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class MemberResource {

    @Autowired
    private OrganizationCapability organizationCapability;

    @Autowired
    private MemberCapability memberCapability;

    /**
     * Get the active status of me
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.ME_PATH)
    public TeamMemberDto getMyCurrentStatus() {
        RequestContext context = RequestContext.get();

        OrganizationMemberEntity memberEntity = organizationCapability.getActiveMembership(context.getRootAccountId());

        return memberCapability.getMyCurrentStatus(memberEntity.getOrganizationId(), memberEntity.getId());
    }

}
