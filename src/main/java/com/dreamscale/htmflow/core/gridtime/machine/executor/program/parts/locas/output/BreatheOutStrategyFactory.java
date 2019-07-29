package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.output;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BreatheOutStrategyFactory {

    @Autowired
    BreatheOutIdeaFlow breatheOutIdeaFlow;

    public BreatheOutStrategy get(BreatheOutType breatheOutType) {
        switch (breatheOutType) {
            case SUMMARIZE_IDEA_FLOW_METRICS:
                return breatheOutIdeaFlow;
        }
        return null;
    }

    public enum BreatheOutType {
        SUMMARIZE_IDEA_FLOW_METRICS,
    }
}
