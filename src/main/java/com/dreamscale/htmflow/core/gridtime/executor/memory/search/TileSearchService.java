package com.dreamscale.htmflow.core.gridtime.executor.memory.search;

import com.dreamscale.htmflow.core.gridtime.executor.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.CarryOverContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TileSearchService {


    public CarryOverContext getCarryOverContextOfTile(UUID torchieId, GeometryClock.Coords gridCoords) {

        //StoryTileEntity tile = storyTileRepository.findByTorchieIdAndDreamTime(torchieId, gridCoords.getFormattedGridTime());

        //TODO clean up persistence of save/retrieving tiles

//        if (tile != null) {
//            StoryTileModel model = JSONTransformer.fromJson(tile.getJsonTile(), StoryTileModel.class);
//            return model.getCarryOverContext();
//        }
        return null;
    }
}
