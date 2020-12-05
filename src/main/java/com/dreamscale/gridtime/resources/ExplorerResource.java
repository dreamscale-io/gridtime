package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.query.GridLocationDto;
import com.dreamscale.gridtime.api.query.LocationInputDto;
import com.dreamscale.gridtime.core.capability.query.ExplorerCapability;
import com.dreamscale.gridtime.core.capability.membership.OrganizationCapability;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.security.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.EXPLORER_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class ExplorerResource {

    @Autowired
    OrganizationCapability organizationCapability;

    @Autowired
    ExplorerCapability explorerCapability;

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.GOTO_PATH)
    public GridLocationDto gotoLocation(@RequestBody LocationInputDto locationInputDto) {
        RequestContext context = RequestContext.get();
        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        log.info("gotoLocation, user={}", invokingMember.getBestAvailableName());

        String terminalCircuitContext = context.getTerminalCircuitContext();

        return explorerCapability.gotoLocation(invokingMember.getOrganizationId(), invokingMember.getId(), terminalCircuitContext, locationInputDto);
    }


    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.LOOK_PATH)
    public GridLocationDto look() {
        RequestContext context = RequestContext.get();
        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        log.info("look, user={}", invokingMember.getBestAvailableName());

        String terminalCircuitContext = context.getTerminalCircuitContext();

        return explorerCapability.look(invokingMember.getOrganizationId(), invokingMember.getId(), terminalCircuitContext);
    }


    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.ZOOM_PATH + ResourcePaths.IN_PATH)
    public GridLocationDto zoomIn() {
        RequestContext context = RequestContext.get();
        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        log.info("zoomIn, user={}", invokingMember.getBestAvailableName());

        String terminalCircuitContext = context.getTerminalCircuitContext();

        return explorerCapability.zoomIn(invokingMember.getOrganizationId(), invokingMember.getId(), terminalCircuitContext);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.ZOOM_PATH + ResourcePaths.OUT_PATH)
    public GridLocationDto zoomOut() {
        RequestContext context = RequestContext.get();
        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        log.info("zoomOut, user={}", invokingMember.getBestAvailableName());

        String terminalCircuitContext = context.getTerminalCircuitContext();

        return explorerCapability.zoomOut(invokingMember.getOrganizationId(), invokingMember.getId(), terminalCircuitContext);
    }


    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.PAN_PATH + ResourcePaths.LEFT_PATH)
    public GridLocationDto panLeft() {
        RequestContext context = RequestContext.get();
        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        log.info("panLeft, user={}", invokingMember.getBestAvailableName());

        String terminalCircuitContext = context.getTerminalCircuitContext();

        return explorerCapability.panLeft(invokingMember.getOrganizationId(), invokingMember.getId(), terminalCircuitContext);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.PAN_PATH + ResourcePaths.RIGHT_PATH)
    public GridLocationDto panRight() {
        RequestContext context = RequestContext.get();
        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        log.info("panLeft, user={}", invokingMember.getBestAvailableName());

        String terminalCircuitContext = context.getTerminalCircuitContext();

        return explorerCapability.panRight(invokingMember.getOrganizationId(), invokingMember.getId(), terminalCircuitContext);
    }
}

