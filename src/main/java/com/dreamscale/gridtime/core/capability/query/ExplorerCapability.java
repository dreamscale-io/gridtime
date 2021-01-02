package com.dreamscale.gridtime.core.capability.query;

import com.dreamscale.gridtime.api.grid.GridTileDto;
import com.dreamscale.gridtime.api.query.TileLocationInputDto;
import com.dreamscale.gridtime.core.capability.active.MemberDetailsRetriever;
import com.dreamscale.gridtime.core.capability.cmd.CommandCapability;
import com.dreamscale.gridtime.core.capability.system.GridClock;
import com.dreamscale.gridtime.core.domain.terminal.TerminalCircuitEntity;
import com.dreamscale.gridtime.core.domain.work.TorchieFeedCursorRepository;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService;
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

    @Autowired
    TileQueryRunner tileQueryRunner;

    @Autowired
    CommandCapability commandCapability;

    public GridTileDto gotoLocation(UUID organizationId, UUID invokingMemberId, String terminalCircuitContext, TileLocationInputDto tileLocationInputDto) {

        LocalDateTime now = gridClock.now();

        TerminalCircuitEntity circuit = terminalCircuitOperator.validateCircuitMembershipAndGetCircuit(organizationId, invokingMemberId, terminalCircuitContext);

        GridtimeExpression tileLocation = GridtimeExpression.parse(tileLocationInputDto.getGridtimeExpression());

        QueryTarget queryTarget = terminalCircuitOperator.resolveQueryTarget(organizationId, invokingMemberId, circuit);

        terminalCircuitOperator.saveLocationHistory(now, organizationId, circuit.getId(), tileLocation);

        return lookupTileAtLocation(queryTarget, tileLocation);
    }

    private GridTileDto lookupTileAtLocation(QueryTarget queryTarget, GridtimeExpression tileLocation) {

        return tileQueryRunner.runQuery(queryTarget, tileLocation);
    }

    private GridTileDto regenerateTileAtLocation(QueryTarget queryTarget, GridtimeExpression tileLocation) {

        return commandCapability.regenerateTile(queryTarget, tileLocation);
    }

    public GridTileDto look(UUID organizationId, UUID invokingMemberId, String terminalCircuitContext) {

        TerminalCircuitEntity circuit = terminalCircuitOperator.validateCircuitMembershipAndGetCircuit(organizationId, invokingMemberId, terminalCircuitContext);

        QueryTarget queryTarget = terminalCircuitOperator.resolveQueryTarget(organizationId, invokingMemberId, circuit);

        GeometryClock.GridTime gtLocation = terminalCircuitOperator.resolveLastLocation(organizationId, invokingMemberId, circuit.getId());

        GridtimeExpression tileLocation = GridtimeExpression.createFrom(gtLocation);

        return lookupTileAtLocation(queryTarget, tileLocation);
    }

    public GridTileDto regenerateTile(UUID organizationId, UUID invokingMemberId, String terminalCircuitContext) {
        TerminalCircuitEntity circuit = terminalCircuitOperator.validateCircuitMembershipAndGetCircuit(organizationId, invokingMemberId, terminalCircuitContext);

        QueryTarget queryTarget = terminalCircuitOperator.resolveQueryTarget(organizationId, invokingMemberId, circuit);

        GeometryClock.GridTime gtLocation = terminalCircuitOperator.resolveLastLocation(organizationId, invokingMemberId, circuit.getId());

        GridtimeExpression tileLocation = GridtimeExpression.createFrom(gtLocation);

        return regenerateTileAtLocation(queryTarget, tileLocation);
    }


    public GridTileDto zoomIn(UUID organizationId, UUID invokingMemberId, String terminalCircuitContext) {

        LocalDateTime now = gridClock.now();

        TerminalCircuitEntity circuit = terminalCircuitOperator.validateCircuitMembershipAndGetCircuit(organizationId, invokingMemberId, terminalCircuitContext);

        QueryTarget queryTarget = terminalCircuitOperator.resolveQueryTarget(organizationId, invokingMemberId, circuit);

        GeometryClock.GridTime gtLocation = terminalCircuitOperator.resolveLastLocation(organizationId, invokingMemberId, circuit.getId());

        GeometryClock.GridTime zoomedInGridtime = gtLocation.zoomIn();

        GridtimeExpression tileLocation = GridtimeExpression.createFrom(zoomedInGridtime);

        terminalCircuitOperator.saveLocationHistory(now, organizationId, circuit.getId(), tileLocation);

        return lookupTileAtLocation(queryTarget, tileLocation);
    }

    public GridTileDto zoomOut(UUID organizationId, UUID invokingMemberId, String terminalCircuitContext) {
        LocalDateTime now = gridClock.now();

        TerminalCircuitEntity circuit = terminalCircuitOperator.validateCircuitMembershipAndGetCircuit(organizationId, invokingMemberId, terminalCircuitContext);

        QueryTarget queryTarget = terminalCircuitOperator.resolveQueryTarget(organizationId, invokingMemberId, circuit);

        GeometryClock.GridTime gtLocation = terminalCircuitOperator.resolveLastLocation(organizationId, invokingMemberId, circuit.getId());

        GeometryClock.GridTime zoomedOutGridtime = gtLocation.zoomOut();

        GridtimeExpression tileLocation = GridtimeExpression.createFrom(zoomedOutGridtime);

        terminalCircuitOperator.saveLocationHistory(now, organizationId, circuit.getId(), tileLocation);

        return lookupTileAtLocation(queryTarget, tileLocation);
    }

    public GridTileDto panLeft(UUID organizationId, UUID invokingMemberId, String terminalCircuitContext) {
        LocalDateTime now = gridClock.now();

        TerminalCircuitEntity circuit = terminalCircuitOperator.validateCircuitMembershipAndGetCircuit(organizationId, invokingMemberId, terminalCircuitContext);

        QueryTarget queryTarget = terminalCircuitOperator.resolveQueryTarget(organizationId, invokingMemberId, circuit);

        GeometryClock.GridTime gtLocation = terminalCircuitOperator.resolveLastLocation(organizationId, invokingMemberId, circuit.getId());

        GeometryClock.GridTime panLeftGridtime = gtLocation.panLeft();

        GridtimeExpression tileLocation = GridtimeExpression.createFrom(panLeftGridtime);

        terminalCircuitOperator.saveLocationHistory(now, organizationId, circuit.getId(), tileLocation);

        return lookupTileAtLocation(queryTarget, tileLocation);
    }

    public GridTileDto panRight(UUID organizationId, UUID invokingMemberId, String terminalCircuitContext) {
        LocalDateTime now = gridClock.now();

        TerminalCircuitEntity circuit = terminalCircuitOperator.validateCircuitMembershipAndGetCircuit(organizationId, invokingMemberId, terminalCircuitContext);

        QueryTarget queryTarget = terminalCircuitOperator.resolveQueryTarget(organizationId, invokingMemberId, circuit);

        GeometryClock.GridTime gtLocation = terminalCircuitOperator.resolveLastLocation(organizationId, invokingMemberId, circuit.getId());

        GeometryClock.GridTime panRightGridtime = gtLocation.panRight();

        GridtimeExpression tileLocation = GridtimeExpression.createFrom(panRightGridtime);

        terminalCircuitOperator.saveLocationHistory(now, organizationId, circuit.getId(), tileLocation);

        return lookupTileAtLocation(queryTarget, tileLocation);
    }


}
