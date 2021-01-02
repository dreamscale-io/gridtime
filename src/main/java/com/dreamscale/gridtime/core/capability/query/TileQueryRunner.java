package com.dreamscale.gridtime.core.capability.query;

import com.dreamscale.gridtime.api.grid.Feature;
import com.dreamscale.gridtime.api.grid.GridTileDto;
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
import com.dreamscale.gridtime.core.machine.memory.grid.cell.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
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


    public GridTileDto runQuery(QueryTarget queryTarget, GridtimeExpression tileLocation) {

        if (tileLocation.getZoomLevel() != ZoomLevel.TWENTY) {
            throw new BadRequestException(ValidationErrorCodes.NO_DATA_AVAILABLE, "Query not yet supported");
        }

        GridCalendarEntity calendar = calendarService.lookupTile(tileLocation.getZoomLevel(), tileLocation.getCoords());

        List<GridRowEntity> rows = gridRowRepository.findByTorchieIdAndCalendarIdOrderByRowIndex(queryTarget.getTargetId(), calendar.getId());

        String title = "Tile "+ tileLocation.getFormattedExpression() + " for "+queryTarget.getTargetType() + " "+ queryTarget.getTargetName();

        log.debug("Retrieving "+title);

        List<Row> rowData = extractRowData(rows);

        GridTileDto gridTileDetails = new GridTileDto();
        gridTileDetails.setLocation(tileLocation.getFormattedExpression());
        gridTileDetails.setTargetType(queryTarget.getTargetType());
        gridTileDetails.setTarget(queryTarget.getTargetName());

        List<String> tileHeaderRow = createTileHeaderRow(tileLocation);

        GridTableResults gridTile = createGridTileResults(title, tileLocation, tileHeaderRow, rowData);
        gridTileDetails.setGridTile(gridTile);
        gridTileDetails.setFeatures(createFeatureTable(gridTile.getTableWidth(), queryTarget.getOrganizationId(), tileHeaderRow, rowData));

        return gridTileDetails;

    }

    private GridTableResults createFeatureTable(int tableWidth, UUID organizationId, List<String> tileHeaderRow, List<Row> rows) {

        Map<UUID, Feature> featureMap = extractFeatures(organizationId, tileHeaderRow, rows);

        return toFeatureTable(tableWidth, featureMap);
    }

    private Map<UUID, Feature> extractFeatures(UUID organizationId, List<String> tileHeaderRow, List<Row> rows) {
        Map<UUID, Feature> featureMap = new LinkedHashMap<>();

        List<String> keys = new ArrayList<>();

        for (int i = 1; i < tileHeaderRow.size(); i++) {
            keys.add(tileHeaderRow.get(i).trim());
        }

        for (int i = 0; i < keys.size(); i++) {
            String column = keys.get(i);
            for (Row row : rows) {
                CellValue cellValue = row.getRowValues().get(column);
                if (cellValue != null) {
                    lookupFeatureAndAddToMap(organizationId, featureMap, row.getRowKey(), column, cellValue);
                }
            }
        }

        return featureMap;
    }

    private List<Row> extractRowData(List<GridRowEntity> rows) {
        List<Row> rowData = new ArrayList<>();

        for (GridRowEntity rowEntity : rows) {
            CellValueMap cellValueMap = JSONTransformer.fromJson(rowEntity.getJson(), CellValueMap.class);
            rowData.add(new Row(rowEntity.getRowName(), cellValueMap));
        }

        return rowData;
    }

    private List<String> createTileHeaderRow(GridtimeExpression tileLocation) {
        List<String> headerRow = new ArrayList<>();
        headerRow.add(CellFormat.toRightSizedCell("ID", CellSize.calculateRowKeyCellSize()));

        MusicClock musicClock = new MusicClock(tileLocation.getZoomLevel());
        Iterator<RelativeBeat> iterator = musicClock.getForwardsIterator();

        while (iterator.hasNext()) {
            RelativeBeat beat = iterator.next();
            headerRow.add(CellFormat.toRightSizedCell(beat.toDisplayString(), CellSize.calculateCellSize(beat)));
        }
        return headerRow;
    }
    private GridTableResults createGridTileResults(String title, GridtimeExpression tileLocation, List<String> headerRow, List<Row> rowData) {

        List<List<String>> rowsWithPaddedCells = new ArrayList<>();

        for (Row row : rowData) {
                List<String> formattedRow = toFormattedRow(tileLocation.getZoomLevel(), row.getRowKey(), row.getRowValues());
            rowsWithPaddedCells.add(formattedRow);
        }

        return new GridTableResults(title, headerRow, rowsWithPaddedCells);

    }

    private GridTableResults toFeatureTable(int tableWidth, Map<UUID, Feature> featureMap) {

        List<List<String>> rows = new ArrayList<>();
        for (Feature feature : featureMap.values()) {
            List<String> formattedRow = toFormattedRow(feature);

            rows.add(formattedRow);
        }

        List<String> headers = new ArrayList<>();
        headers.add("Ref");
        headers.add("Feature");
        headers.add("Description");

        String titleAsHorizontalRule = createHorizontalRule(tableWidth);

        RightSizer.rightSizeCells(headers, rows, tableWidth);

        return new GridTableResults(titleAsHorizontalRule, headers, rows);
    }

    private String createHorizontalRule(int tableWidth) {
        StringBuilder builder = new StringBuilder(tableWidth);

        for (int i = 0; i < tableWidth - 1; i++) {
            builder.append('-');
        }

        return builder.toString();
    }

    private List<String> toFormattedRow(Feature feature) {
        List<String> row = new ArrayList<>();

        String ref = feature.getFirstReference();
        if (feature.getFeatureGlyph() != null) {
            ref += "("+feature.getFeatureGlyph() + ")";
        }

        row.add(CellFormat.toCellValue(ref));
        row.add(CellFormat.toCellValue(feature.getFeatureType()));
        row.add(CellFormat.toCellValue(feature.getDescription()));

        return row;
    }

    private void lookupFeatureAndAddToMap(UUID organizationId, Map<UUID, Feature> featureMap, String rowKey, String column, CellValue cellValue) {
        List<UUID> refs = cellValue.getRefs();
        List<String> glyphs = cellValue.getGlyphRefs();

        if (refs != null) {
            for (int i = 0; i < refs.size(); i++) {
                UUID ref = refs.get(i);
                if (!featureMap.containsKey(ref) && isExpandableType(rowKey)) {
                    FeatureReference reference = featureResolverService.lookupById(organizationId, ref);
                    if (reference != null ) {
                        String glyph = null;
                        if (glyphs != null && i < glyphs.size()) {
                            glyph = glyphs.get(i);
                        }
                        Feature feature = new Feature(ref, reference.getFeatureType().getTypeUri(), glyph, reference.getDescription(), column);

                        featureMap.put(ref, feature);
                    } else {
                        log.warn("Unable to map feature ref in column "+ column + " value: "+ cellValue.getVal());
                    }
                }
            }
        }
    }

    private boolean isExpandableType(String rowKey) {
        if (rowKey.contains("@flow") ) {
            return false;
        }
        return true;
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

    @AllArgsConstructor
    @Getter
    private class Row {
        String rowKey;
        CellValueMap rowValues;
    }

}
