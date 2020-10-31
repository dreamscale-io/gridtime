package com.dreamscale.gridtime.api.grid;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GridStatusSummaryDto implements Results {

    GridStatus gridStatus;
    String message;

    GridTableResults activitySummary;

    @Override
    public String toDisplayString() {
        String gridOutput = gridStatus + " : "+message + "\n";

        gridOutput += activitySummary.toDisplayString();

        return gridOutput;
    }
}
