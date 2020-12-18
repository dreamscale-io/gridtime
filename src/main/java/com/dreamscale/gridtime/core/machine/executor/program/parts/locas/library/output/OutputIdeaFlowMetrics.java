package com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.output;

import com.dreamscale.gridtime.core.domain.tile.metrics.GridIdeaFlowMetricsEntity;
import com.dreamscale.gridtime.core.domain.tile.metrics.GridIdeaFlowMetricsRepository;
import com.dreamscale.gridtime.core.domain.tile.GridRowEntity;
import com.dreamscale.gridtime.core.domain.tile.GridRowRepository;
import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.commons.JSONTransformer;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.CellValueMap;
import com.dreamscale.gridtime.core.machine.memory.grid.query.metrics.IdeaFlowMetrics;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService;
import com.dreamscale.gridtime.core.machine.memory.grid.IMusicGrid;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.CellValue;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.GridRow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class OutputIdeaFlowMetrics implements OutputStrategy {

    @Autowired
    CalendarService calendarService;

    @Autowired
    GridIdeaFlowMetricsRepository gridIdeaFlowMetricsRepository;

    @Autowired
    GridRowRepository gridRowRepository;

    @Override
    public int breatheOut(UUID torchieId, Metronome.TickScope tickScope, IMusicGrid musicGrid) {

        IdeaFlowMetrics ideaFlowMetrics = IdeaFlowMetrics.queryFrom(musicGrid);

        UUID calendarId = calendarService.lookupCalendarId(tickScope.getFrom());

        GridIdeaFlowMetricsEntity metricsEntity = createIdeaFlowMetricsEntity(torchieId, calendarId, ideaFlowMetrics);
        gridIdeaFlowMetricsRepository.save(metricsEntity);


        List<GridRowEntity> rowEntities = new ArrayList<>();
        for (GridRow row: musicGrid.getAllGridRows()) {
            GridRowEntity rowEntity = createRowEntityIfNotEmpty(torchieId, tickScope.getZoomLevel(), calendarId, row);
            if (rowEntity != null) {
                rowEntities.add(rowEntity);
            }
        }
        gridRowRepository.save(rowEntities);

        return rowEntities.size();
    }

    private GridRowEntity createRowEntityIfNotEmpty(UUID torchieId, ZoomLevel zoomLevel, UUID calendarId, GridRow row) {
        GridRowEntity gridRowEntity = null;

       CellValueMap cellValueMap = row.toCellValueMap();

        if (cellValueMap.size() > 0) {

            gridRowEntity = new GridRowEntity();
            gridRowEntity.setId(UUID.randomUUID());
            gridRowEntity.setRowName(row.getRowKey().getName());
            gridRowEntity.setTorchieId(torchieId);
            gridRowEntity.setCalendarId(calendarId);
            gridRowEntity.setJson(JSONTransformer.toJson(cellValueMap));

            log.debug(row.getRowKey().getName() + ":" +gridRowEntity.getJson());
        }

        return gridRowEntity;
    }

    private GridIdeaFlowMetricsEntity createIdeaFlowMetricsEntity(UUID torchieId, UUID calendarId, IdeaFlowMetrics ideaFlowMetrics) {
        GridIdeaFlowMetricsEntity entity = new GridIdeaFlowMetricsEntity();

        entity.setId(UUID.randomUUID());
        entity.setTorchieId(torchieId);
        entity.setCalendarId(calendarId);
        entity.setAvgFlame(ideaFlowMetrics.getAvgFlame());
        entity.setPercentWtf(ideaFlowMetrics.getPercentWtf());
        entity.setPercentLearning(ideaFlowMetrics.getPercentLearning());
        entity.setPercentProgress(ideaFlowMetrics.getPercentProgress());
        entity.setPercentPairing(ideaFlowMetrics.getPercentPairing());
        entity.setTimeInTile(ideaFlowMetrics.getTimeInTile().getSeconds());
        return entity;
    }
}
