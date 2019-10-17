package com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.input;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InputStrategyFactory {

    @Autowired
    InputIdeaFlowMetricsAcrossTime inputIdeaFlow;

    @Autowired
    InputIdeaFlowMetricsAcrossTeam inputIdeaFlowMetricsAcrossTeam;

    @Autowired
    InputBoxMetricsAcrossTime inputBoxMetricsAcrossTime;

    @Autowired
    InputBoxMetricsAcrossTeam inputBoxMetricsAcrossTeam;

    public <T> InputStrategy<T> get(InputType inputType) {
        switch (inputType) {
            case QUERY_IDEA_FLOW_METRICS:
                return (InputStrategy<T>) inputIdeaFlow;
            case QUERY_TEAM_IDEA_FLOW_METRICS:
                return (InputStrategy<T>) inputIdeaFlowMetricsAcrossTeam;
            case QUERY_BOX_METRICS:
                return (InputStrategy<T>) inputBoxMetricsAcrossTime;
            case QUERY_TEAM_BOX_METRICS:
                return (InputStrategy<T>) inputBoxMetricsAcrossTeam;
        }
        return null;
    }

    public enum InputType {
        QUERY_IDEA_FLOW_METRICS,
        QUERY_TEAM_IDEA_FLOW_METRICS,
        QUERY_BOX_METRICS,
        QUERY_TEAM_BOX_METRICS,

    }
}
