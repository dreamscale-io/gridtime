package com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.see;

import com.dreamscale.htmflow.core.gridtime.executor.alarm.TimeBombTrigger;
import org.springframework.stereotype.Component;

import java.util.LinkedList;

@Component
public class CircleCapabilities {


    public <S> TimeBombTrigger refreshWTFsForThesePlaces(LinkedList<S> similarsOneHopAway) {
        return null;
    }

    public <S> TimeBombTrigger refreshWithPeersAroundThisOrigin(S centerOfCircle) {
        return null;
    }

    public <S> TimeBombTrigger refreshWithChildrenOfThisOrigin(S centerOfCircle) {
        return null;
    }
}
