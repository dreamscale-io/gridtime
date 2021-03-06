package com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.output;

import com.dreamscale.gridtime.core.domain.tile.metrics.GridBoxMetricsEntity;
import com.dreamscale.gridtime.core.domain.tile.metrics.GridBoxMetricsRepository;
import com.dreamscale.gridtime.core.domain.tile.GridRowEntity;
import com.dreamscale.gridtime.core.domain.tile.GridRowRepository;
import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.CellValueMap;
import com.dreamscale.gridtime.core.machine.memory.grid.query.metrics.BoxMetrics;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService;
import com.dreamscale.gridtime.core.machine.commons.JSONTransformer;
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
public class OutputBoxMetrics implements OutputStrategy {

    @Autowired
    CalendarService calendarService;

    @Autowired
    GridBoxMetricsRepository gridBoxMetricsRepository;

    @Autowired
    GridRowRepository gridRowRepository;

    @Override
    public int breatheOut(UUID torchieId, Metronome.TickScope tickScope, IMusicGrid musicGrid) {

        List<BoxMetrics> boxMetrics = BoxMetrics.queryFrom(musicGrid);

        UUID calendarId = calendarService.lookupCalendarId(tickScope.getFrom());

        List<GridBoxMetricsEntity> entities = createEntities(torchieId, calendarId, boxMetrics);
        gridBoxMetricsRepository.save(entities);


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

        CellValueMap rowValues = row.toCellValueMap();

        if (rowValues.size() > 0) {

            gridRowEntity = new GridRowEntity();
            gridRowEntity.setId(UUID.randomUUID());
            gridRowEntity.setRowName(row.getRowKey().getName());
            gridRowEntity.setTorchieId(torchieId);
            gridRowEntity.setCalendarId(calendarId);
            gridRowEntity.setJson(JSONTransformer.toJson(rowValues));

            log.debug(row.getRowKey().getName() + ":" +gridRowEntity.getJson());
        }

        return gridRowEntity;
    }

    private List<GridBoxMetricsEntity> createEntities(UUID torchieId, UUID calendarId, List<BoxMetrics> boxMetricsList) {

        List<GridBoxMetricsEntity> entities = new ArrayList<>();

        for (BoxMetrics metrics : boxMetricsList) {
            entities.add(createGridBoxMetricsEntity(torchieId, calendarId, metrics));
        }

        return entities;
    }

    private GridBoxMetricsEntity createGridBoxMetricsEntity(UUID torchieId, UUID calendarId, BoxMetrics boxMetrics) {
        GridBoxMetricsEntity boxMetricsEntity = new GridBoxMetricsEntity();

        boxMetricsEntity.setId(UUID.randomUUID());
        boxMetricsEntity.setTorchieId(torchieId);
        boxMetricsEntity.setCalendarId(calendarId);

        boxMetricsEntity.setBoxFeatureId(boxMetrics.getBox().getFeatureId());
        boxMetricsEntity.setTimeInBox(boxMetrics.getTimeInBox().getSeconds());

        boxMetricsEntity.setAvgFlame(boxMetrics.getAvgFlame());
        boxMetricsEntity.setPercentWtf(boxMetrics.getPercentWtf());
        boxMetricsEntity.setPercentLearning(boxMetrics.getPercentLearning());
        boxMetricsEntity.setPercentProgress(boxMetrics.getPercentProgress());
        boxMetricsEntity.setPercentPairing(boxMetrics.getPercentPairing());
        boxMetricsEntity.setAvgFileBatchSize(boxMetrics.getAvgFileBatchSize());
        boxMetricsEntity.setAvgExecutionTime(boxMetrics.getAvgExecutionTime());
        boxMetricsEntity.setAvgTraversalSpeed(boxMetrics.getAvgTraversalSpeed());

        return boxMetricsEntity;
    }
}
