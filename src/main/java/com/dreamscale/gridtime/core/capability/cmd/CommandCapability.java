package com.dreamscale.gridtime.core.capability.cmd;

import com.dreamscale.gridtime.api.grid.GridTileDto;
import com.dreamscale.gridtime.api.query.TargetType;
import com.dreamscale.gridtime.core.capability.query.GridtimeExpression;
import com.dreamscale.gridtime.core.capability.query.QueryTarget;
import com.dreamscale.gridtime.core.capability.query.TileQueryRunner;
import com.dreamscale.gridtime.core.domain.time.GridCalendarEntity;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.machine.GridTimeEngine;
import com.dreamscale.gridtime.core.machine.capabilities.cmd.TorchieCmd;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommandCapability {

    @Autowired
    GridTimeEngine gridTimeEngine;

    @Autowired
    TileQueryRunner tileQueryRunner;

    @Autowired
    CalendarService calendarService;

    public GridTileDto regenerateTile(QueryTarget queryTarget, GridtimeExpression tileLocation) {

        if (queryTarget.getTargetType() != TargetType.USER) {
            throw new BadRequestException(ValidationErrorCodes.INVALID_COMMAND_PARAMETERS, "Only regeneration of user tiles is currently supported");
        }

        TorchieCmd torchieCmd = gridTimeEngine.getTorchieCmd(queryTarget.getTargetId());

        GridCalendarEntity calendarTile = calendarService.lookupTile(tileLocation.getZoomLevel(), tileLocation.getCoords());
        torchieCmd.regenerateTile(tileLocation.getZoomLevel(), calendarTile.getStartTime());

        gridTimeEngine.releaseTorchieCmd(torchieCmd);
        return tileQueryRunner.runQuery(queryTarget, tileLocation);
    }
}
