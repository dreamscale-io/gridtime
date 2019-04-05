package com.dreamscale.htmflow.core.feeds.executor.parts.mapper;

public class RadialClock {

    private final int slots;
    private final double angleBeat;

    public RadialClock(int slots) {
        this.slots = slots;
        this.angleBeat = 360.0 / slots;
    }

    public double getAngleOfSlot(int slotNumber) {
        return slotNumber * angleBeat;
    }

    public int getNearestSlot(double angle) {
        return (int)Math.round(angle / angleBeat);
    }

}
