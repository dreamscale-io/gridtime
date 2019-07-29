package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.input;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BreatheInStrategyFactory {

    @Autowired
    BreatheInIdeaFlow breatheInIdeaFlow;

    public <T> BreatheInStrategy<T> get(BreatheInType breatheInType) {
        switch (breatheInType) {
            case QUERY_IDEA_FLOW_METRICS:
                return (BreatheInStrategy<T>)breatheInIdeaFlow;
        }
        return null;
    }

    public enum BreatheInType {
        QUERY_IDEA_FLOW_METRICS,
    }
}
