package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.fetch.tile;

import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.clock.ZoomLevel;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.analytics.query.FeatureMetrics;

import java.util.List;
import java.util.UUID;

public class TileQuery {

    UUID queryByTorchieId;
    GeometryClock.GridTime queryByGridTime;

    List<FeatureMetrics> queryFeatures() {
        return null;
    }



    //so I've got the 20s, that I need to aggregate into a daypart.

    //I've got a specific day part that I query by, and query against the 20s for all the 20s tiles matching the day part.

    //then I find all the torchie tile summary metrics within the range.

}
