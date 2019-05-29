package com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.see;

import com.dreamscale.htmflow.core.gridtime.executor.alarm.TimeBombTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SeeFromPlaceCapabilities {

    @Autowired
    private CircleCapabilities circleCapabilities;

    @Autowired
    private TracerArrowCapabilities tracerArrowCapabilities;


    public TimeBombTrigger refreshWTFsWithinThisPlace(SeeFromPlace seeFromPlace) {
        return null;
    }

    public CircleCapabilities getCircleCapabilities() {
        return circleCapabilities;
    }

    public TracerArrowCapabilities getTracerArrowCapabilities() {
        return tracerArrowCapabilities;
    }
}
