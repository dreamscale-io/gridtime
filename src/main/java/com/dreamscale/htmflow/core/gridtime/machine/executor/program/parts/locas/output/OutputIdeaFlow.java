package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.output;

import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.AggregateGrid;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OutputIdeaFlow implements OutputStrategy {


    @Override
    public void breatheOut(UUID torchieId, Metronome.Tick tick, AggregateGrid aggregateGrid) {


       // IdeaFlowMetrics.queryFrom(aggregateGrid);
    }
}
