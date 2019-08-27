package com.dreamscale.gridtime.core.machine.executor.circuit.alarm;

import com.dreamscale.gridtime.core.machine.clock.RelativeBeat;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.memory.type.CmdType;
import lombok.Getter;

@Getter
public class TimeBomb {

    private final ZoomLevel zoomLevel;
    private int fullMeasuresRemainingToCountDown;
    private final RelativeBeat beatWithinMeasureToSplode;

    private final AlarmScript alarmScript;

    public TimeBomb(ZoomLevel zoomLevel, int fullMeasuresRemainingToCountDown, RelativeBeat beatWithinMeasureToSplode) {
        this.zoomLevel = zoomLevel;
        this.fullMeasuresRemainingToCountDown = fullMeasuresRemainingToCountDown;
        this.beatWithinMeasureToSplode = beatWithinMeasureToSplode;
        this.alarmScript = new AlarmScript();
    }

    public boolean countDown() {
        if (fullMeasuresRemainingToCountDown > 0) {
            fullMeasuresRemainingToCountDown--;
        }

        return isSplodeWithinMeasure();
    }

    public void addOnAlarmInstruction(CmdType cmdType, String argName, String argValue) {
        alarmScript.addInstruction(cmdType, argName, argValue);
    }

    public RelativeBeat getFutureSplodingBeat() {
        return beatWithinMeasureToSplode;
    }

    public boolean isSplodeWithinBeat(RelativeBeat currentBeat) {
        return beatWithinMeasureToSplode.isWithin(currentBeat);
    }

    public boolean isSplodeWithinMeasure() {
        if (fullMeasuresRemainingToCountDown == 0) {
            return true;
        } else {
            return false;
        }
    }



}
