package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.sink;

import com.dreamscale.htmflow.core.domain.tile.*;
import com.dreamscale.htmflow.core.gridtime.machine.memory.TorchieState;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.GridTile;
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
    public void save(UUID torchieId, TorchieState torchieState) {

        GridTile gridTile = torchieState.getActiveTile();

        TorchieBookmarkEntity latestBookmark = bookmarkRepository.findByTorchieId(torchieId);

        if (latestBookmark == null) {
            latestBookmark = new TorchieBookmarkEntity();
            latestBookmark.setTorchieId(torchieId);
        }

        LocalDateTime startOfNextTile = gridTile.getGridTime().panRight().getClockTime();
        latestBookmark.setMetronomeCursor(startOfNextTile);

        bookmarkRepository.save(latestBookmark);
    }


}
