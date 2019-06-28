package com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.query.aggregate;

public class AverageMetric {

    int total;
    int sampleCount;

    public void addSample(int sample) {
        total += sample;
        sampleCount++;
    }

    public Double getAverage() {
        if (sampleCount > 0) {
            return total * 1.0 / sampleCount;
        } else {
            return 0.0;
        }
    }

}
