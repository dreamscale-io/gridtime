package com.dreamscale.gridtime.core.capability.query;

import com.dreamscale.gridtime.api.query.TileLocationInputDto;
import com.dreamscale.gridtime.api.grid.GridTableResults;
import com.dreamscale.gridtime.core.capability.active.MemberDetailsRetriever;
import com.dreamscale.gridtime.core.capability.system.GridClock;
import com.dreamscale.gridtime.core.domain.circuit.CircuitMemberEntity;
import com.dreamscale.gridtime.core.domain.member.MemberDetailsEntity;
import com.dreamscale.gridtime.core.domain.terminal.TerminalCircuitEntity;
import com.dreamscale.gridtime.core.domain.terminal.TerminalCircuitLocationHistoryEntity;
import com.dreamscale.gridtime.core.domain.terminal.TerminalCircuitLocationHistoryRepository;
import com.dreamscale.gridtime.core.domain.work.TorchieFeedCursorEntity;
import com.dreamscale.gridtime.core.domain.work.TorchieFeedCursorRepository;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.machine.clock.GridtimeSequence;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class ExplorerCapability {

    @Autowired
    CalendarService calendarService;

    @Autowired
    GridClock gridClock;

    @Autowired
    MemberDetailsRetriever memberDetailsRetriever;

    @Autowired
    TorchieFeedCursorRepository torchieFeedCursorRepository;

    @Autowired
    TerminalCircuitOperator terminalCircuitOperator;


    public GridTableResults gotoLocation(UUID organizationId, UUID memberId, String terminalCircuitContext, TileLocationInputDto tileLocationInputDto) {

        LocalDateTime now = gridClock.now();

        TerminalCircuitEntity circuit = terminalCircuitOperator.validateCircuitMembershipAndGetCircuit(organizationId, memberId, terminalCircuitContext);

        GridtimeExpression tileLocation = GridtimeExpression.parse(tileLocationInputDto.getGridtimeExpression());

        if (tileLocation.hasRangeExpression()) {
            throw new BadRequestException(ValidationErrorCodes.INVALID_GT_EXPRESSION, "Tile location must be specific and not a range.");
        }

        terminalCircuitOperator.saveLocationHistory(now, organizationId, circuit.getId(), tileLocation);

        return lookupTileAtLocationn(tileLocation);
    }

    private GridTableResults lookupTileAtLocationn(GridtimeExpression tileLocation) {

        //first, lets start with the 20 min tiles, and reconstructing the table on the screen

        //Ive got the rows with different track types

        //currently these row types don't have an order.  I can give them one in the tables, so they sort consistently

        //lets start with this

        //then I think I want to do lookup tables as support commands, so tracks can have details?
        //maybe save off these feature maps, and then the feature maps are organized by row?
        //some will just be a set of things.  Aggregated by type perhaps.  Need to figure out execution metrics.

        return null;
    }

    public GridTableResults look(UUID organizationId, UUID invokingMemberId, String terminalCircuitContext) {


        return null;
    }


    public GridTableResults zoomIn(UUID organizationId, UUID memberId, String terminalCircuitContext) {
        return null;
    }

    public GridTableResults zoomOut(UUID organizationId, UUID memberId, String terminalCircuitContext) {
        return null;
    }

    public GridTableResults panLeft(UUID organizationId, UUID memberId, String terminalCircuitContext) {
        return null;
    }

    public GridTableResults panRight(UUID organizationId, UUID memberId, String terminalCircuitContext) {
        return null;
    }
}
