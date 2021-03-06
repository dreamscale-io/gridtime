package com.dreamscale.gridtime.core.machine.capabilities.browse;

import com.dreamscale.gridtime.core.machine.executor.circuit.alarm.TimeBomb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SeeFromPlaceCapabilities {

    @Autowired
    private CircleCapabilities circleCapabilities;

    @Autowired
    private TracerArrowCapabilities tracerArrowCapabilities;


    public TimeBomb refreshWTFsWithinThisPlace(SeeFromPlace seeFromPlace) {
        return null;
    }

    public CircleCapabilities getCircleCapabilities() {
        return circleCapabilities;
    }

    public TracerArrowCapabilities getTracerArrowCapabilities() {
        return tracerArrowCapabilities;
    }
}
