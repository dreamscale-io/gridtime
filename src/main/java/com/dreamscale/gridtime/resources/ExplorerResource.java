package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.grid.GridTileDto;
import com.dreamscale.gridtime.api.query.TileLocationInputDto;
import com.dreamscale.gridtime.api.terminal.ActivityContext;
import com.dreamscale.gridtime.api.terminal.Command;
import com.dreamscale.gridtime.core.capability.query.ExplorerCapability;
import com.dreamscale.gridtime.core.capability.membership.OrganizationCapability;
import com.dreamscale.gridtime.core.capability.terminal.TerminalRoute;
import com.dreamscale.gridtime.core.capability.terminal.TerminalRouteRegistry;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.security.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.EXPLORER_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class ExplorerResource {

    @Autowired
    OrganizationCapability organizationCapability;

    @Autowired
    ExplorerCapability explorerCapability;

    @Autowired
    TerminalRouteRegistry terminalRouteRegistry;

    @PostConstruct
    void init() {

        terminalRouteRegistry.register(ActivityContext.TILES, Command.GOTO,
                "Goto a specific gridtime (gt) location",
                new GotoTileTerminalRoute());

        terminalRouteRegistry.register(ActivityContext.TILES, Command.LOOK,
                "View the tile at the current gridtime (gt) location",
                new GotoTileTerminalRoute());

        terminalRouteRegistry.register(ActivityContext.TILES, Command.ZOOM,
                "Zoom in or out of the current gridtime (gt) location",
                new ZoomTileTerminalRoute());

        terminalRouteRegistry.register(ActivityContext.TILES, Command.PAN,
                "Pan left or right from the current gridtime (gt) location",
                new PanTileTerminalRoute());

    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.GOTO_PATH)
    public GridTileDto gotoLocation(@RequestBody TileLocationInputDto tileLocationInputDto) {
        RequestContext context = RequestContext.get();
        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        log.info("gotoLocation, user={}", invokingMember.getBestAvailableName());

        String terminalCircuitContext = context.getTerminalCircuitContext();

        return explorerCapability.gotoLocation(invokingMember.getOrganizationId(), invokingMember.getId(), terminalCircuitContext, tileLocationInputDto);
    }


    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.LOOK_PATH)
    public GridTileDto look() {
        RequestContext context = RequestContext.get();
        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        log.info("look, user={}", invokingMember.getBestAvailableName());

        String terminalCircuitContext = context.getTerminalCircuitContext();

        return explorerCapability.look(invokingMember.getOrganizationId(), invokingMember.getId(), terminalCircuitContext);
    }


    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.ZOOM_PATH + ResourcePaths.IN_PATH)
    public GridTileDto zoomIn() {
        RequestContext context = RequestContext.get();
        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        log.info("zoomIn, user={}", invokingMember.getBestAvailableName());

        String terminalCircuitContext = context.getTerminalCircuitContext();

        return explorerCapability.zoomIn(invokingMember.getOrganizationId(), invokingMember.getId(), terminalCircuitContext);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.ZOOM_PATH + ResourcePaths.OUT_PATH)
    public GridTileDto zoomOut() {
        RequestContext context = RequestContext.get();
        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        log.info("zoomOut, user={}", invokingMember.getBestAvailableName());

        String terminalCircuitContext = context.getTerminalCircuitContext();

        return explorerCapability.zoomOut(invokingMember.getOrganizationId(), invokingMember.getId(), terminalCircuitContext);
    }


    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.PAN_PATH + ResourcePaths.LEFT_PATH)
    public GridTileDto panLeft() {
        RequestContext context = RequestContext.get();
        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        log.info("panLeft, user={}", invokingMember.getBestAvailableName());

        String terminalCircuitContext = context.getTerminalCircuitContext();

        return explorerCapability.panLeft(invokingMember.getOrganizationId(), invokingMember.getId(), terminalCircuitContext);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.PAN_PATH + ResourcePaths.RIGHT_PATH)
    public GridTileDto panRight() {
        RequestContext context = RequestContext.get();
        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        log.info("panLeft, user={}", invokingMember.getBestAvailableName());

        String terminalCircuitContext = context.getTerminalCircuitContext();

        return explorerCapability.panRight(invokingMember.getOrganizationId(), invokingMember.getId(), terminalCircuitContext);
    }


    private class GotoTileTerminalRoute extends TerminalRoute {

        private static final String TILE_PARAM = "tile";

        GotoTileTerminalRoute() {
            super(Command.GOTO, "{" + TILE_PARAM + "}");
            describeTextOption(TILE_PARAM, "gt[location] for a specific tile");
        }

        @Override
        public Object route(Map<String, String> params) {
            String gtLocation = params.get(TILE_PARAM);

            return gotoLocation(new TileLocationInputDto(gtLocation));
        }
    }

    private class ZoomTileTerminalRoute extends TerminalRoute {

        private static final String DIRECTION_PARAM = "direction";
        private static final String CHOICE_IN = "in";
        private static final String CHOICE_OUT = "out";

        ZoomTileTerminalRoute() {
            super(Command.ZOOM, "{" + DIRECTION_PARAM + "}");
            describeChoiceOption(DIRECTION_PARAM, CHOICE_IN, CHOICE_OUT);
        }

        @Override
        public Object route(Map<String, String> params) {
            String zoomDirection = params.get(DIRECTION_PARAM);

            if (zoomDirection.equalsIgnoreCase(CHOICE_IN)) {
                return zoomIn();
            } else if (zoomDirection.equalsIgnoreCase(CHOICE_OUT)) {
                return zoomOut();
            } else {
                throw new BadRequestException(ValidationErrorCodes.UNABLE_TO_FIND_TERMINAL_ROUTE, "Valid zoom choices are 'in' or 'out'");
            }
        }
    }

    private class PanTileTerminalRoute extends TerminalRoute {

        private static final String DIRECTION_PARAM = "direction";
        private static final String CHOICE_LEFT = "left";
        private static final String CHOICE_RIGHT = "right";

        PanTileTerminalRoute() {
            super(Command.PAN, "{" + DIRECTION_PARAM + "}");
            describeChoiceOption(DIRECTION_PARAM, CHOICE_LEFT, CHOICE_RIGHT);
        }

        @Override
        public Object route(Map<String, String> params) {
            String panDirection = params.get(DIRECTION_PARAM);

            if (panDirection.equalsIgnoreCase(CHOICE_LEFT)) {
                return panLeft();
            } else if (panDirection.equalsIgnoreCase(CHOICE_RIGHT)) {
                return panRight();
            } else {
                throw new BadRequestException(ValidationErrorCodes.UNABLE_TO_FIND_TERMINAL_ROUTE, "Valid pan choices are 'left' or 'right'");
            }
        }
    }

    private class LookTileTerminalRoute extends TerminalRoute {

        LookTileTerminalRoute() {
            super(Command.LOOK, "");
        }

        @Override
        public Object route(Map<String, String> params) {
            return look();
        }
    }
}

