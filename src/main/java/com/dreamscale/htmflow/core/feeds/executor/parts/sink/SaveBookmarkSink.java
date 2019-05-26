package com.dreamscale.htmflow.core.feeds.executor.parts.sink;

import com.dreamscale.htmflow.core.domain.tile.*;
import com.dreamscale.htmflow.core.feeds.clock.ZoomLevel;
import com.dreamscale.htmflow.core.feeds.story.TileBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
@Slf4j
public class SaveBookmarkSink implements SinkStrategy {

    @Autowired
    TorchieBookmarkRepository bookmarkRepository;

    @Override
    public void save(UUID torchieId, TileBuilder tileBuilder) {

        TorchieBookmarkEntity latestBookmark = bookmarkRepository.findByTorchieId(torchieId);

        if (latestBookmark == null) {
            latestBookmark = new TorchieBookmarkEntity();
            latestBookmark.setTorchieId(torchieId);
        }

        LocalDateTime startOfNextTile = tileBuilder.getTileCoordinates().panRight(ZoomLevel.TWENTIES).getClockTime();
        latestBookmark.setMetronomeCursor(startOfNextTile);

        bookmarkRepository.save(latestBookmark);

    }


}
