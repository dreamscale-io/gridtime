package com.dreamscale.gridtime.core.capability.query;

import com.dreamscale.gridtime.api.grid.GridTableResults;
import com.dreamscale.gridtime.api.query.TargetType;
import com.dreamscale.gridtime.core.capability.active.MemberDetailsRetriever;
import com.dreamscale.gridtime.core.domain.circuit.LearningCircuitEntity;
import com.dreamscale.gridtime.core.domain.circuit.LearningCircuitRepository;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.CellFormat;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.RightSizer;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class TopWTFsQueryRunner {


    @Autowired
    LearningCircuitRepository learningCircuitRepository;

    @Autowired
    MemberDetailsRetriever memberDetailsRetriever;

    private static final int TOP_QUERY_LIMIT = 10;

    public GridTableResults runQuery(QueryTarget queryTarget, QueryTimeScope queryTimeScope) {

        List<LearningCircuitEntity> topWtfs = Collections.emptyList();
        if (queryTarget.getTargetType().equals(TargetType.USER)) {
            topWtfs = learningCircuitRepository.findTopWTFsForMemberInTimeRange(
                    queryTarget.getOrganizationId(), queryTarget.getTargetId(),
                    Timestamp.valueOf(queryTimeScope.getStartTime()),
                    Timestamp.valueOf(queryTimeScope.getEndTime()), TOP_QUERY_LIMIT);

        } else if (queryTarget.getTargetType().equals(TargetType.TEAM)) {
            topWtfs = learningCircuitRepository.findTopWTFsAcrossTeamInTimeRange(
                    queryTarget.getOrganizationId(), queryTarget.getTargetId(),
                    Timestamp.valueOf(queryTimeScope.getStartTime()),
                    Timestamp.valueOf(queryTimeScope.getEndTime()), TOP_QUERY_LIMIT);
        }

        return toGridTableResults("Top WTFs in " + queryTimeScope.getScopeDescription() + " for "
                + queryTarget.getTargetType().name() + " "
                + queryTarget.getTargetName() , topWtfs);
    }


    private GridTableResults toGridTableResults(String title, List<LearningCircuitEntity> topWtfs) {

        List<String> headerRow = createHeaderRow();

        List<List<String>> valueRows = new ArrayList<>();

        for (LearningCircuitEntity wtf : topWtfs) {
            List<String> row = createRow(wtf);

            valueRows.add(row);
        }

        RightSizer.rightSizeCells(headerRow, valueRows);

        return new GridTableResults(title, headerRow, valueRows);
    }



    List<String> createHeaderRow() {
        List<String> row = new ArrayList<>();

        row.add("Circuit");
        row.add("User");
        row.add("Coords");
        row.add("Day");
        row.add("Timer(h:m:s)");
        row.add("Status");
        row.add("Description");

        return row;
    }

    List<String> createRow(LearningCircuitEntity wtf) {
        List<String> row = new ArrayList<>();

        String circuitLink = "/wtf/" + wtf.getCircuitName();
        String username = memberDetailsRetriever.lookupUsername(wtf.getOwnerId());

        String duration = DurationFormatUtils.formatDuration(wtf.getTotalCircuitElapsedNanoTime() / 1000000, "HH:mm:ss", true);

        String circuitState = wtf.getCircuitState().name();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd");

        String openTime = formatter.format(wtf.getOpenTime());

        String openGridTime = GeometryClock.createGridTime(ZoomLevel.TWENTY, wtf.getOpenTime()).getFormattedCoords();

        String description = wtf.getDescription();

        row.add(CellFormat.toCellValue(circuitLink));
        row.add(CellFormat.toCellValue(username));
        row.add(CellFormat.toCellValue(openGridTime));
        row.add(CellFormat.toCellValue(openTime));
        row.add(CellFormat.toCellValue(duration));
        row.add(CellFormat.toCellValue(circuitState));
        row.add(CellFormat.toCellValue(CellFormat.toRightSizedCell(description, 40)));

        return row;
    }


}
