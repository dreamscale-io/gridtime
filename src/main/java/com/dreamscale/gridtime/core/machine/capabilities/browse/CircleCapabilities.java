package com.dreamscale.gridtime.core.machine.capabilities.browse;

import com.dreamscale.gridtime.core.machine.executor.circuit.alarm.TimeBomb;
import org.springframework.stereotype.Component;

import java.util.LinkedList;

@Component
public class CircleCapabilities {


    public <S> TimeBomb refreshWTFsForThesePlaces(LinkedList<S> similarsOneHopAway) {
        return null;
    }

    public <S> TimeBomb refreshWithPeersAroundThisOrigin(S centerOfCircle) {
        return null;
    }

    public <S> TimeBomb refreshWithChildrenOfThisOrigin(S centerOfCircle) {
        return null;
    }
}
