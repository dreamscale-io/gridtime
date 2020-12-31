package com.dreamscale.gridtime.api.grid;

import com.dreamscale.gridtime.api.query.TargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GridLocation implements Results {

    TargetType targetType;
    String target;
    String gridtime;

    GridTableResults tileResults;

    List<Feature> features;

    @Override
    public String toDisplayString() {
        return tileResults.toDisplayString();
    }
}
