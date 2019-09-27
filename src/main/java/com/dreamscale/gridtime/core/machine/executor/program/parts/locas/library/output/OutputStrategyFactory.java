package com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.output;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OutputStrategyFactory {

    @Autowired
    OutputIdeaFlowMetrics outputIdeaFlowMetrics;

    @Autowired
    OutputBoxMetrics outputBoxMetrics;

    public OutputStrategy get(OutputType outputType) {
        switch (outputType) {
            case SUMMARIZE_IDEA_FLOW_METRICS:
                return outputIdeaFlowMetrics;
            case SUMMARIZE_BOX_METRICS:
                return outputBoxMetrics;
        }
        return null;
    }

    public enum OutputType {
        SUMMARIZE_IDEA_FLOW_METRICS,
        SUMMARIZE_BOX_METRICS,
    }
}
