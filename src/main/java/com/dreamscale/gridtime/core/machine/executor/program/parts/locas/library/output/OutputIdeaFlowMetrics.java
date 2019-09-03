package com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.output;

import com.dreamscale.gridtime.core.domain.tile.GridIdeaFlowMetricsEntity;
import com.dreamscale.gridtime.core.domain.tile.GridIdeaFlowMetricsRepository;
import com.dreamscale.gridtime.core.domain.tile.GridRowEntity;
import com.dreamscale.gridtime.core.domain.tile.GridRowRepository;
import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.commons.JSONTransformer;
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
    public void breatheOut(UUID torchieId, Metronome.Tick tick, IMusicGrid musicGrid) {

        IdeaFlowMetrics ideaFlowMetrics = IdeaFlowMetrics.queryFrom(musicGrid);

        Long tileSeq = calendarService.lookupTileSequenceNumber(tick.getFrom());

        GridIdeaFlowMetricsEntity metricsEntity = createIdeaFlowMetricsEntity(torchieId, tileSeq, ideaFlowMetrics);
        gridIdeaFlowMetricsRepository.save(metricsEntity);


        List<GridRowEntity> rowEntities = new ArrayList<>();
        for (GridRow row: musicGrid.getAllGridRows()) {
            GridRowEntity rowEntity = createRowEntityIfNotEmpty(torchieId, tick.getZoomLevel(), tileSeq, row);
            if (rowEntity != null) {
                rowEntities.add(rowEntity);
            }
        }
        gridRowRepository.save(rowEntities);

    }

    private GridRowEntity createRowEntityIfNotEmpty(UUID torchieId, ZoomLevel zoomLevel, Long tileSeq, GridRow row) {
        GridRowEntity gridRowEntity = null;

        Map<String, CellValue> rowValues = row.toCellValueMap();

        if (rowValues.size() > 0) {

            gridRowEntity = new GridRowEntity();
            gridRowEntity.setId(UUID.randomUUID());
            gridRowEntity.setRowName(row.getRowKey().getName());
            gridRowEntity.setTorchieId(torchieId);
            gridRowEntity.setZoomLevel(zoomLevel);
            gridRowEntity.setTileSeq(tileSeq);
            gridRowEntity.setJson(JSONTransformer.toJson(rowValues));

            log.debug(row.getRowKey().getName() + ":" +gridRowEntity.getJson());
        }

        return gridRowEntity;
    }

    private GridIdeaFlowMetricsEntity createIdeaFlowMetricsEntity(UUID torchieId, Long tileSeq, IdeaFlowMetrics ideaFlowMetrics) {
        GridIdeaFlowMetricsEntity entity = new GridIdeaFlowMetricsEntity();

        entity.setId(UUID.randomUUID());
        entity.setTorchieId(torchieId);
        entity.setZoomLevel(ideaFlowMetrics.getZoomLevel());
        entity.setTileSeq(tileSeq);
        entity.setAvgFlame(ideaFlowMetrics.getAvgFlame());
        entity.setPercentWtf(ideaFlowMetrics.getPercentWtf());
        entity.setPercentLearning(ideaFlowMetrics.getPercentLearning());
        entity.setPercentProgress(ideaFlowMetrics.getPercentProgress());
        entity.setPercentPairing(ideaFlowMetrics.getPercentPairing());
        return entity;
    }
}
