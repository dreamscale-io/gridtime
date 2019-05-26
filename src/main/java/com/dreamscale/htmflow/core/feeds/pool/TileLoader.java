package com.dreamscale.htmflow.core.feeds.pool;

import com.dreamscale.htmflow.core.domain.tile.StoryTileEntity;
import com.dreamscale.htmflow.core.domain.tile.StoryTileRepository;
import com.dreamscale.htmflow.core.feeds.clock.GeometryClock;
import com.dreamscale.htmflow.core.feeds.clock.ZoomLevel;
import com.dreamscale.htmflow.core.feeds.executor.parts.sink.JSONTransformer;
import com.dreamscale.htmflow.core.feeds.story.StoryTileModel;
import com.dreamscale.htmflow.core.feeds.story.feature.CarryOverContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TileLoader {

    @Autowired
    StoryTileRepository storyTileRepository;

    public CarryOverContext getContextOfPreviousTile(UUID torchieId, GeometryClock.Coords storyTileCoords, ZoomLevel zoomLevel) {
        GeometryClock.Coords previousCoords = storyTileCoords.panLeft(zoomLevel);

        StoryTileEntity tile = storyTileRepository.findByTorchieIdAndDreamTime(torchieId, previousCoords.formatDreamTime());

        if (tile != null) {
            StoryTileModel model = JSONTransformer.fromJson(tile.getJsonTile(), StoryTileModel.class);
            return model.getCarryOverContext();
        }
        return null;
    }
}
