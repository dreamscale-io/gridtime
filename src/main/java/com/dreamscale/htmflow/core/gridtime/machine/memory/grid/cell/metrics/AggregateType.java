package com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.metrics;

public enum AggregateType {
    MIN("Calc[Min]"),
    MAX("Calc[Max]"),
    TOTAL("Calc[Tot]"),
    AVG("Calc[Avg]"),
    STDDEV("Calc[Std]"),
    COUNT("Calc[Cnt]");

    private final String header;

    AggregateType(String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
    }

    public String toString() {
        return name();
    }
}
