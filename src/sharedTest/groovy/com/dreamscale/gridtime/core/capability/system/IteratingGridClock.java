package com.dreamscale.gridtime.core.capability.system;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
public class IteratingGridClock extends GridClock {

    private LocalDateTime currentClock;
    private Long currentNano;
    private int ticks;

    private static final Long START_NANO = 444444444444444L;

    public IteratingGridClock() {
        reset();
    }

    public void reset() {
        currentClock = getGridStart();
        currentNano = START_NANO;
        ticks = 0;
    }

    public Long getStartNano() {
        return START_NANO;
    }

    public int getTicks() {
        return ticks;
    }

    @Override
    public LocalDateTime now() {
        LocalDateTime time = currentClock;
        log.debug("Iterating Clock: "+time);
        tick();

        return time;
    }


    @Override
    public Long nanoTime() {
        return currentNano;
    }

    private void tick() {
        ticks++;
        currentClock = currentClock.plusMinutes(1);
        currentNano += 6000000000L;
    }
}
