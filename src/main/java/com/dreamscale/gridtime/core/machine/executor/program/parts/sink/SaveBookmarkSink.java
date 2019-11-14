package com.dreamscale.gridtime.core.machine.executor.program.parts.sink;

import com.dreamscale.gridtime.core.domain.tile.*;
import com.dreamscale.gridtime.core.machine.executor.worker.BookmarkService;
import com.dreamscale.gridtime.core.machine.memory.TorchieState;
import com.dreamscale.gridtime.core.machine.memory.tile.GridTile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
@Slf4j
public class SaveBookmarkSink implements SinkStrategy {

    @Autowired
    BookmarkService bookmarkService;

    @Override
    public void save(UUID torchieId, TorchieState torchieState) {

        GridTile gridTile = torchieState.getActiveTile();

        bookmarkService.updateLastProcessed(torchieId, gridTile.getGridTime().getClockTime());
        
    }


}
