package com.dreamscale.htmflow.core.gridtime.kernel.executor.circuit.alarm;

import com.dreamscale.htmflow.core.gridtime.kernel.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.ZoomLevel;
import lombok.Getter;

@Getter
public class TimeBombTrigger {

    private final ZoomLevel zoomLevel;
    private int fullMeasuresRemainingToCountDown;
    private final RelativeBeat beatWithinMeasureToSplode;

    private Trigger trigger;

    public TimeBombTrigger(ZoomLevel zoomLevel, int fullMeasuresRemainingToCountDown, RelativeBeat beatWithinMeasureToSplode) {
        this.zoomLevel = zoomLevel;
        this.fullMeasuresRemainingToCountDown = fullMeasuresRemainingToCountDown;
        this.beatWithinMeasureToSplode = beatWithinMeasureToSplode;
    }

    public void wireUpTrigger(Trigger trigger) {
        this.trigger = trigger;
    }

    public void fireTrigger() {
        trigger.fire();
    }

    public boolean countDownFullMeasure() {
        if (fullMeasuresRemainingToCountDown > 0) {
            fullMeasuresRemainingToCountDown--;
        }

        return isWithinMeasure();
    }

    public RelativeBeat getFutureSplodingBeat() {
        return beatWithinMeasureToSplode;
    }

    public boolean isWithinSplodingBeat(RelativeBeat currentBeat) {
        return beatWithinMeasureToSplode.isWithin(currentBeat);
    }

    public boolean isWithinMeasure() {
        if (fullMeasuresRemainingToCountDown == 0) {
            return true;
        } else {
            return false;
        }
    }



}
