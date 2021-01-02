package com.dreamscale.gridtime.api.grid;

import com.dreamscale.gridtime.api.query.TargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GridTileDto implements Results {

    TargetType targetType;
    String target;
    String location;

    GridTableResults gridTile;
    GridTableResults features;

    @Override
    public String toDisplayString() {
        return gridTile.toDisplayString() + features.toDisplayString().substring(1);
    }

    private int size() {
        return gridTile.size() + features.size();
    }

    @Override
    public String toString() {
        return toDisplayString();
    }

}
