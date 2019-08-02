package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.library.output;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OutputStrategyFactory {

    @Autowired
    OutputIdeaFlow outputIdeaFlow;

    public OutputStrategy get(OutputType outputType) {
        switch (outputType) {
            case SUMMARIZE_IDEA_FLOW_METRICS:
                return outputIdeaFlow;
        }
        return null;
    }

    public enum OutputType {
        SUMMARIZE_IDEA_FLOW_METRICS,
    }
}
