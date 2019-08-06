package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.library.input;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InputStrategyFactory {

    @Autowired
    MemberInputIdeaFlow inputIdeaFlow;

    @Autowired
    TeamInputIdeaFlow teamInputIdeaFlow;

    public <T> InputStrategy<T> get(InputType inputType) {
        switch (inputType) {
            case QUERY_IDEA_FLOW_METRICS:
                return (InputStrategy<T>) inputIdeaFlow;
            case QUERY_TEAM_IDEA_FLOW_METRICS:
                return (InputStrategy<T>) teamInputIdeaFlow;
        }
        return null;
    }

    public enum InputType {
        QUERY_IDEA_FLOW_METRICS,
        QUERY_TEAM_IDEA_FLOW_METRICS,

    }
}
