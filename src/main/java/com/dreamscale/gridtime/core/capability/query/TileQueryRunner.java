package com.dreamscale.gridtime.core.capability.query;

import com.dreamscale.gridtime.api.grid.Feature;
import com.dreamscale.gridtime.api.grid.GridLocation;
import com.dreamscale.gridtime.api.grid.GridTableResults;
import com.dreamscale.gridtime.core.capability.active.MemberDetailsRetriever;
import com.dreamscale.gridtime.core.domain.circuit.LearningCircuitRepository;
import com.dreamscale.gridtime.core.domain.tile.GridMarkerRepository;
import com.dreamscale.gridtime.core.domain.tile.GridRowEntity;
import com.dreamscale.gridtime.core.domain.tile.GridRowRepository;
import com.dreamscale.gridtime.core.domain.tile.metrics.GridBoxMetricsRepository;
import com.dreamscale.gridtime.core.domain.tile.metrics.GridIdeaFlowMetricsRepository;
import com.dreamscale.gridtime.core.domain.time.GridCalendarEntity;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.machine.clock.*;
import com.dreamscale.gridtime.core.machine.commons.JSONTransformer;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureResolverService;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.CellFormat;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.CellSize;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.CellValue;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.CellValueMap;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class TileQueryRunner {


    @Autowired
    CalendarService calendarService;

    @Autowired
    LearningCircuitRepository learningCircuitRepository;

    @Autowired
    MemberDetailsRetriever memberDetailsRetriever;

    @Autowired
    GridRowRepository gridRowRepository;

    @Autowired
    GridMarkerRepository gridMarkerRepository;

    @Autowired
    GridIdeaFlowMetricsRepository gridIdeaFlowMetricsRepository;

    @Autowired
    GridBoxMetricsRepository gridBoxMetricsRepository;

    @Autowired
    FeatureResolverService featureResolverService;


    public GridTableResults runQuery(QueryTarget queryTarget, GridtimeExpression tileLocation) {

        if (tileLocation.getZoomLevel() != ZoomLevel.TWENTY) {
            throw new BadRequestException(ValidationErrorCodes.NO_DATA_AVAILABLE, "Query not yet supported");
        }

        GridCalendarEntity calendar = calendarService.lookupTile(tileLocation.getZoomLevel(), tileLocation.getCoords());

        List<GridRowEntity> rows = gridRowRepository.findByTorchieIdAndCalendarIdOrderByRowIndex(queryTarget.getTargetId(), calendar.getId());

        String title = "Tile "+ tileLocation.getFormattedExpression() + " for "+queryTarget.getTargetType() + " "+ queryTarget.getTargetName();

        log.debug("Retrieving "+title);

        GridLocation location = createGridLocation(title, queryTarget.getOrganizationId(), tileLocation, rows);

        return location.getTileResults();

    }

    private GridLocation createGridLocation(String title, UUID organizationId, GridtimeExpression tileLocation, List<GridRowEntity> rows) {

        GridLocation gridLocation = new GridLocation();

        List<String> headerRow = new ArrayList<>();
        headerRow.add(CellFormat.toRightSizedCell("ID", CellSize.calculateRowKeyCellSize()));

        MusicClock musicClock = new MusicClock(tileLocation.getZoomLevel());
        Iterator<RelativeBeat> iterator = musicClock.getForwardsIterator();

        while (iterator.hasNext()) {
            RelativeBeat beat = iterator.next();
            headerRow.add(CellFormat.toRightSizedCell(beat.toDisplayString(), CellSize.calculateCellSize(beat)));
        }

        List<List<String>> rowsWithPaddedCells = new ArrayList<>();

        //let me change the data type to a subclass, and then test the serialization next

        Map<UUID, Feature> featureMap = new LinkedHashMap<>();

        for (GridRowEntity rowEntity : rows) {
                CellValueMap cellValueMap = JSONTransformer.fromJson(rowEntity.getJson(), CellValueMap.class);

                lookupFeaturesAndAddToMap(organizationId, featureMap, cellValueMap);

                List<String> formattedRow = toFormattedRow(tileLocation.getZoomLevel(), rowEntity.getRowName(), cellValueMap);
            rowsWithPaddedCells.add(formattedRow);

        }

        GridTableResults tileResults = new GridTableResults(title, headerRow, rowsWithPaddedCells);
        gridLocation.setTileResults(tileResults);

        GridTableResults featureTable = toFeatureTable(featureMap);
        
        return gridLocation;
    }

    private GridTableResults toFeatureTable(Map<UUID, Feature> featureMap) {

        return null;
    }

    private Map<UUID, Feature> lookupFeaturesAndAddToMap(UUID organizationId, Map<UUID, Feature> featureMap, CellValueMap cellValueMap) {

        for (Map.Entry<String, CellValue> cellEntry : cellValueMap.getRowValues().entrySet()) {
            List<UUID> refs = cellEntry.getValue().getRefs();
            List<String> glyphs = cellEntry.getValue().getGlyphRefs();

            if (refs != null) {
                for (int i = 0; i < refs.size(); i++) {
                    UUID ref = refs.get(i);
                    if (!featureMap.containsKey(ref)) {
                        FeatureReference reference = featureResolverService.lookupById(organizationId, ref);
                        if (reference != null) {
                            String glyph = null;
                            if (glyphs != null && i < glyphs.size()) {
                                glyph = glyphs.get(i);
                            }
                            Feature feature = new Feature(ref, reference.getFeatureType().name(), glyph, reference.getDescription(), cellEntry.getKey());

                            featureMap.put(ref, feature);
                        } else {
                            log.warn("Unable to map feature ref in column "+cellEntry.getKey() + " value: "+cellEntry.getValue().getVal());
                        }
                    }
                }
            }
        }
        return featureMap;

    }

    private List<String> toFormattedRow(ZoomLevel zoomLevel, String rowName, CellValueMap cellValueMap) {

        List<String> rowValues = new ArrayList<>();
        rowValues.add(CellFormat.toRightSizedCell(rowName, CellSize.calculateRowKeyCellSize()));

        int numBeats = zoomLevel.getInnerBeats();

        if (cellValueMap.size() > 0) {
            String firstKey = cellValueMap.getFirstKey();
            numBeats = getBeatsPerMeasureFromKey(firstKey);
        }

        Beat[] beats = Beat.getFlyweightBeats(numBeats);

        for (int i = 0; i < beats.length; i++) {
            Beat beat = beats[i];
            CellValue valueObj = cellValueMap.get(beat.toDisplayString());
            String value = "";

            if (valueObj != null) {
                value = valueObj.getVal();
            }
            rowValues.add(CellFormat.toRightSizedCell(value, CellSize.calculateCellSize(beat)));
        }

        return rowValues;
    }

    private int getBeatsPerMeasureFromKey(String firstKey) {
        String numberBeats = firstKey.substring(0, firstKey.indexOf('.'));

        return Integer.parseInt(numberBeats);
    }


}
