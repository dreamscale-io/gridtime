package com.dreamscale.gridtime.core.machine.executor.program.parts.sink;

import com.dreamscale.gridtime.core.domain.tile.*;
import com.dreamscale.gridtime.core.domain.tile.metrics.GridBoxMetricsEntity;
import com.dreamscale.gridtime.core.domain.tile.metrics.GridBoxMetricsRepository;
import com.dreamscale.gridtime.core.domain.tile.metrics.GridIdeaFlowMetricsEntity;
import com.dreamscale.gridtime.core.domain.tile.metrics.GridIdeaFlowMetricsRepository;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.commons.JSONTransformer;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService;
import com.dreamscale.gridtime.core.machine.memory.TorchieState;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.CellValue;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.CellValueMap;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.GridRow;
import com.dreamscale.gridtime.core.machine.memory.grid.query.metrics.BoxMetrics;
import com.dreamscale.gridtime.core.machine.memory.grid.query.metrics.IdeaFlowMetrics;
import com.dreamscale.gridtime.core.machine.memory.tag.FeatureTag;
import com.dreamscale.gridtime.core.machine.memory.tile.GridTile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class SaveToPostgresSink implements SinkStrategy {

    @Autowired
    CalendarService calendarService;

    @Autowired
    GridRowRepository gridRowRepository;

    @Autowired
    GridMarkerRepository gridMarkerRepository;

    @Autowired
    GridIdeaFlowMetricsRepository gridIdeaFlowMetricsRepository;

    @Autowired
    GridBoxMetricsRepository gridBoxMetricsRepository;

    @Override
    public void save(UUID torchieId, TorchieState torchieState) {

        GridTile gridTile = torchieState.getActiveTile();

        //first thing I need to do, is save the wtf banded row.

        UUID calendarId = calendarService.lookupCalendarId(gridTile.getGridTime());

        List<GridRow> allGridRows = gridTile.getAllGridRows();

        List<GridRowEntity> rowEntities = new ArrayList<>();
        for (int i = 0; i < allGridRows.size(); i++) {
            GridRow row = allGridRows.get(i);
            GridRowEntity rowEntity = createRowEntityIfNotEmpty(torchieId, gridTile.getZoomLevel(), calendarId, row, i);
            if (rowEntity != null) {
                rowEntities.add(rowEntity);
            }
        }
        gridRowRepository.save(rowEntities);

        //okay markers, what do we need to do for this?

        List<GridMarkerEntity> markerEntities = new ArrayList<>();

        for (GridRow row: allGridRows) {
            List<GridMarkerEntity> rowMarkers = createRowMarkerEntitiesIfNotEmpty(torchieId, calendarId, row);
            if (rowMarkers != null) {
                markerEntities.addAll(rowMarkers);
            }
        }
        gridMarkerRepository.save(markerEntities);


        IdeaFlowMetrics ideaFlowMetrics = gridTile.getIdeaFlowMetrics();
        log.debug("wtf percent: "+ ideaFlowMetrics.getPercentWtf());
        log.debug("ideaflowMetrics: "+ ideaFlowMetrics);

        GridIdeaFlowMetricsEntity gridIdeaFlowMetricsEntity = createGridIdeaFlowMetricsEntity(torchieId, calendarId, ideaFlowMetrics);

        gridIdeaFlowMetricsRepository.save(gridIdeaFlowMetricsEntity);


        List<BoxMetrics> boxMetricsList = gridTile.getBoxMetrics();
        List<GridBoxMetricsEntity> boxMetricsEntityList = new ArrayList<>();

        for (BoxMetrics boxMetrics : boxMetricsList) {

            GridBoxMetricsEntity gridBoxMetricsEntity = createGridBoxMetricsEntity(torchieId, calendarId, boxMetrics);
            boxMetricsEntityList.add(gridBoxMetricsEntity);
            log.debug("box: "+gridBoxMetricsEntity);
        }

        log.debug("Saving BoxMetrics, count: "+boxMetricsEntityList.size());
        gridBoxMetricsRepository.save(boxMetricsEntityList);

        //box metrics, each tile can have multiple boxes... max of all boxes per tile

        //then when I aggregate, need to do it by box...
        //so I've got a DayPart tile, get all the boxes across all the 12 subtiles, and group by box

        //so each row is a box reference

        //@box/K
        //@box/F

        //then my columns are the metrics?  So, wtf percent, tile,


        //TILE DICTIONARY...

        //so rows will have all these rhythm patterns... bridge patterns... need to add bridges, okay added bridges
        //instead of feature references being in rows, save the lookup table

        //we want to be able to do union distinct on dictionaries

        //okay tile summary... what do we need to do fo

        //TODO save lookup tables

        //wtf and wtf^ reference UUID
        //execution, do I need the featureIds? (yes)

        //TODO save box analytics

        //TODO save bridge analytics

        //TODO update floating now

        //TODO aggregate up
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

    private GridIdeaFlowMetricsEntity createGridIdeaFlowMetricsEntity(UUID torchieId, UUID calendarId, IdeaFlowMetrics ideaFlowMetrics) {

        GridIdeaFlowMetricsEntity ideaFlowEntity = new GridIdeaFlowMetricsEntity();
        ideaFlowEntity.setId(UUID.randomUUID());
        ideaFlowEntity.setTorchieId(torchieId);
        ideaFlowEntity.setCalendarId(calendarId);
        ideaFlowEntity.setAvgFlame(ideaFlowMetrics.getAvgFlame());
        ideaFlowEntity.setTimeInTile(ideaFlowMetrics.getTimeInTile().getSeconds());
        ideaFlowEntity.setPercentWtf(ideaFlowMetrics.getPercentWtf());
        ideaFlowEntity.setPercentLearning(ideaFlowMetrics.getPercentLearning());
        ideaFlowEntity.setPercentProgress(ideaFlowMetrics.getPercentProgress());
        ideaFlowEntity.setPercentPairing(ideaFlowMetrics.getPercentPairing());
        return ideaFlowEntity;
    }

    private List<GridMarkerEntity> createRowMarkerEntitiesIfNotEmpty(UUID torchieId, UUID calendarId, GridRow row) {

        List<GridMarkerEntity> markerEntities = null;

        List<FeatureTag<?>> featureTags = row.getFeatureTags();

        if (featureTags != null && featureTags.size() > 0) {
            markerEntities = new ArrayList<>();

            for (FeatureTag<?> featureTag : featureTags) {
                GridMarkerEntity markerEntity = new GridMarkerEntity();
                markerEntity.setId(UUID.randomUUID());
                markerEntity.setTorchieId(torchieId);
                markerEntity.setCalendarId(calendarId);
                markerEntity.setRowName(row.getRowKey().getName());
                markerEntity.setPosition(featureTag.getPosition());
                markerEntity.setTagType(featureTag.getTag().getType());
                markerEntity.setTagName(featureTag.getTag().name());
                markerEntity.setGridFeatureId(featureTag.getFeature().getFeatureId());

                markerEntities.add(markerEntity);
            }
        }

        return markerEntities;
    }

    private GridRowEntity createRowEntityIfNotEmpty(UUID torchieId, ZoomLevel zoomLevel, UUID calendarId, GridRow row, int rowIndex) {
        GridRowEntity gridRowEntity = null;

        CellValueMap cellValueMap = row.toCellValueMap();

        if (cellValueMap.size() > 0) {

            gridRowEntity = new GridRowEntity();
            gridRowEntity.setId(UUID.randomUUID());
            gridRowEntity.setRowName(row.getRowKey().getName());
            gridRowEntity.setTorchieId(torchieId);
            gridRowEntity.setCalendarId(calendarId);
            gridRowEntity.setRowIndex(rowIndex);
            gridRowEntity.setJson(JSONTransformer.toJson(cellValueMap));

            log.debug(row.getRowKey().getName() + ":" +gridRowEntity.getJson());
        }

        return gridRowEntity;
    }

}
