package com.dreamscale.gridtime.core.capability.system;

import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class GridClock {

    private LocalDateTime gridStart;

    public GridClock() {
        this.gridStart = GeometryClock.getFirstMomentOfYear(2020);
    }

    public LocalDateTime now() {
        LocalDateTime now = LocalDateTime.now();
        return now;
    }

    public Long nanoTime() {

        return System.currentTimeMillis() * 1000000;
    }

    public LocalDateTime getGridStart() {
        return gridStart;
    }
}
