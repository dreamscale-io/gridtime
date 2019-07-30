package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.input;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InputStrategyFactory {

    @Autowired
    InputIdeaFlow inputIdeaFlow;

    public <T> InputStrategy<T> get(InputType inputType) {
        switch (inputType) {
            case QUERY_IDEA_FLOW_METRICS:
                return (InputStrategy<T>) inputIdeaFlow;
        }
        return null;
    }

    public enum InputType {
        QUERY_IDEA_FLOW_METRICS,
    }
}
