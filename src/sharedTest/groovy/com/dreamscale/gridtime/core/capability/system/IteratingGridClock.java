package com.dreamscale.gridtime.core.capability.system;

import java.time.LocalDateTime;

public class IteratingGridClock extends GridClock {

    private LocalDateTime currentClock;
    private Long currentNano;
    private int ticks;

    private static final LocalDateTime START_CLOCK = LocalDateTime.of(2020, 4, 20, 11, 11);
    private static final Long START_NANO = 444444444444444L;

    public IteratingGridClock() {
        reset();
    }

    public void reset() {
        currentClock = START_CLOCK;
        currentNano = START_NANO;
        ticks = 0;
    }

    public LocalDateTime getStartClock() {
        return START_CLOCK;
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
