package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.sink;

import com.dreamscale.htmflow.core.domain.tile.*;
import com.dreamscale.htmflow.core.gridtime.machine.clock.ZoomLevel;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.service.CalendarService;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.CellValue;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.GridRow;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.query.IdeaFlowMetrics;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tag.FeatureTag;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.GridTile;
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
    GridTileIdeaFlowRepository gridTileIdeaFlowRepository;

    @Override
    public void save(UUID torchieId, GridTile gridTile) {

        //first thing I need to do, is save the wtf banded row.

        Long tileSeq = calendarService.lookupTileSequenceNumber(gridTile.getGridTime());

        List<GridRow> allGridRows = gridTile.getMusicGrid().getAllGridRows();

        List<GridRowEntity> rowEntities = new ArrayList<>();
        for (GridRow row: allGridRows) {
            GridRowEntity rowEntity = createRowEntityIfNotEmpty(torchieId, gridTile.getZoomLevel(), tileSeq, row);
            if (rowEntity != null) {
                rowEntities.add(rowEntity);
            }
        }
        gridRowRepository.save(rowEntities);

        //okay markers, what do we need to do for this?

        List<GridMarkerEntity> markerEntities = new ArrayList<>();

        for (GridRow row: allGridRows) {
            List<GridMarkerEntity> rowMarkers = createRowMarkerEntitiesIfNotEmpty(torchieId, tileSeq, row);
            if (rowMarkers != null) {
                markerEntities.addAll(rowMarkers);
            }
        }
        gridMarkerRepository.save(markerEntities);


        IdeaFlowMetrics ideaFlowMetrics = gridTile.getIdeaFlowMetrics();

        GridTileIdeaFlowEntity gridTileIdeaFlowEntity = createGridTileIdeaFlowEntity(torchieId, tileSeq, ideaFlowMetrics);
        gridTileIdeaFlowRepository.save(gridTileIdeaFlowEntity);

        //TILE DICTIONARY...

        //so rows will have all these rhythm patterns... bridge patterns... need to add bridges, okay added bridges
        //instead of feature references being in rows, save the lookup table

        //we want to be able to do union distinct on dictionaries

        //okay tile summary... what do we need to do fo

        //TODO save lookup tables

        //wtf and wtf^ reference UUID
        //execution, do I need the featureIds? (yes)

        //TODO save box metrics

        //TODO save bridge metrics

        //TODO update floating now

        //TODO aggregate up
    }

    private GridTileIdeaFlowEntity createGridTileIdeaFlowEntity(UUID torchieId, Long tileSeq, IdeaFlowMetrics ideaFlowMetrics) {

        GridTileIdeaFlowEntity ideaFlowEntity = new GridTileIdeaFlowEntity();
        ideaFlowEntity.setId(UUID.randomUUID());
        ideaFlowEntity.setTorchieId(torchieId);
        ideaFlowEntity.setTileSeq(tileSeq);
        ideaFlowEntity.setZoomLevel(ideaFlowMetrics.getZoomLevel());
        ideaFlowEntity.setLastIdeaFlowState(ideaFlowMetrics.getLastIdeaFlowState().getFeatureId());
        ideaFlowEntity.setAvgFlame(ideaFlowMetrics.getAvgFlame());
        ideaFlowEntity.setTimeInTile(ideaFlowMetrics.getTimeInTile().getSeconds());
        ideaFlowEntity.setPercentWtf(ideaFlowMetrics.getPercentWtf());
        ideaFlowEntity.setPercentLearning(ideaFlowMetrics.getPercentLearning());
        ideaFlowEntity.setPercentProgress(ideaFlowMetrics.getPercentProgress());
        ideaFlowEntity.setPercentPairing(ideaFlowMetrics.getPercentPairing());
        return ideaFlowEntity;
    }

    private List<GridMarkerEntity> createRowMarkerEntitiesIfNotEmpty(UUID torchieId, Long tileSeq, GridRow row) {

        List<GridMarkerEntity> markerEntities = null;

        List<FeatureTag<?>> featureTags = row.getFeatureTags();

        if (featureTags != null && featureTags.size() > 0) {
            markerEntities = new ArrayList<>();

            for (FeatureTag<?> featureTag : featureTags) {
                GridMarkerEntity markerEntity = new GridMarkerEntity();
                markerEntity.setId(UUID.randomUUID());
                markerEntity.setTorchieId(torchieId);
                markerEntity.setTileSeq(tileSeq);
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

}
