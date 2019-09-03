package com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service;

import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.memory.grid.query.metrics.BoxMetrics;
import com.dreamscale.gridtime.core.machine.memory.grid.query.metrics.IdeaFlowMetrics;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeatureId;
import com.dreamscale.gridtime.core.machine.memory.tile.CarryOverContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class TileSearchService {


    public CarryOverContext getCarryOverContextOfTile(UUID torchieId, GeometryClock.GridTime gridGridTime) {

        //StoryTileEntity tile = storyTileRepository.findByTorchieIdAndDreamTime(torchieId, gridCoords.getFormattedGridTime());

        //TODO clean up persistence of save/retrieving tiles

//        if (tile != null) {
//            StoryTileModel model = JSONTransformer.fromJson(tile.getJsonTile(), StoryTileModel.class);
//            return model.getCarryOverContext();
//        }
        return null;
    }

    List<IdeaFlowMetrics> queryTiles(UUID torchieId, GeometryClock.GridTime queryByGridTime) {
        //zoom in one level for the same feed
        return null;
    }

    List<FeatureId> queryFeatureSet(UUID torchieId, GeometryClock.GridTime queryByGridTime) {
        //zoom in one level for the same feed, get all the feature reference objects, bookmark by what...?

        //can do 10 featureIds at a time
        return null;
    }

    List<BoxMetrics> queryBoxMetrics(UUID torchieId, FeatureId featureId, GeometryClock.GridTime queryByGridTime) {
        //public List<IdeaFlowTile>

        //these are atomic operations.  FeatureMetrics for the new tile, are combined all at once, and then closed.
        //If the feature is present, it includes all features.


        //then can do a list of metrics at a time, or maybe I just join?  How do I know which features I've aggregated?
        return null;
    }

}
