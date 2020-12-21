package com.dreamscale.gridtime.core.capability.query;

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
import com.dreamscale.gridtime.core.machine.memory.grid.cell.CellFormat;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.CellSize;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.CellValue;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.CellValueMap;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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


    public GridTableResults runQuery(QueryTarget queryTarget, GridtimeExpression tileLocation) {

        if (tileLocation.getZoomLevel() != ZoomLevel.TWENTY) {
            throw new BadRequestException(ValidationErrorCodes.NO_DATA_AVAILABLE, "Query not yet supported");
        }

        GridCalendarEntity calendar = calendarService.lookupTile(tileLocation.getZoomLevel(), tileLocation.getCoords());

        List<GridRowEntity> rows = gridRowRepository.findByTorchieIdAndCalendarIdOrderByRowIndex(queryTarget.getTargetId(), calendar.getId());

        String title = "Tile "+ tileLocation.getFormattedExpression() + " for "+queryTarget.getTargetType() + " "+ queryTarget.getTargetName();


        return createTable(title, tileLocation, rows);

    }

    private GridTableResults createTable(String title, GridtimeExpression tileLocation, List<GridRowEntity> rows) {
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
        for (GridRowEntity rowEntity : rows) {
                CellValueMap cellValueMap = JSONTransformer.fromJson(rowEntity.getJson(), CellValueMap.class);
                List<String> formattedRow = toFormattedRow(tileLocation.getZoomLevel(), rowEntity.getRowName(), cellValueMap);
            rowsWithPaddedCells.add(formattedRow);

        }

        return new GridTableResults(title, headerRow, rowsWithPaddedCells);
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
