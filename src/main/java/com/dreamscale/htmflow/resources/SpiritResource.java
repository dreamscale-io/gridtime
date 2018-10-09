package com.dreamscale.htmflow.resources;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.batch.NewFlowBatch;
import com.dreamscale.htmflow.api.event.NewSnippetEvent;
import com.dreamscale.htmflow.api.status.XPSummaryDto;
import com.dreamscale.htmflow.core.domain.OrganizationMemberEntity;
import com.dreamscale.htmflow.core.security.RequestContext;
import com.dreamscale.htmflow.core.service.FlowService;
import com.dreamscale.htmflow.core.service.OrganizationService;
import com.dreamscale.htmflow.core.service.XPService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.SPIRIT_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class SpiritResource {

    @Autowired
    XPService xpService;

    @Autowired
    OrganizationService organizationService;

    /**
     * Saves a batch of flow activity and events for the user
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.XP_PATH)
    public XPSummaryDto getLatestXP() {
        RequestContext context = RequestContext.get();
        log.info("getLatestXP, user={}", context.getMasterAccountId());

        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(context.getMasterAccountId());

        return xpService.getLatestXPForMember(memberEntity.getId());
    }

}
