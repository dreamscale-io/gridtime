package com.dreamscale.htmflow.core.gridtime.kernel.executor.program.parts.sink;

import com.dreamscale.htmflow.core.domain.tile.*;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.ZoomLevel;
import com.dreamscale.htmflow.core.gridtime.kernel.executor.program.parts.service.CalendarService;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.CellValue;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.GridRow;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tag.FeatureTag;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tile.GridTile;
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

        //okay tile summary... what do we need to do fo

        GridTileSummaryEntity summaryEntity = new GridTileSummaryEntity();
        summaryEntity.setId(UUID.randomUUID());
        summaryEntity.setTorchieId(torchieId);
        summaryEntity.setZoomLevel(gridTile.getZoomLevel());
        summaryEntity.setTileSeq(tileSeq);



//        private Integer timeInTile;
//        private Integer timeInWtf;
//        private Integer timeInLearning;
//        private Integer timeInProgress;
//        private Integer timeInPairing;
//
//        private Float avgFlame;
//        private Float avgBatchSize;
//        private Float avgTraversalSpeed;
//        private Float avgExecutionTime;
//        private Float avgRedToGreenTime;

        //TODO save tile summary

        //TODO save lookup tables

        //wtf and wtf^ reference UUID
        //execution, do I need the featureIds? (yes)

        //TODO save box metrics

        //TODO save bridge metrics

        //TODO update floating now

        //TODO aggregate up
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
