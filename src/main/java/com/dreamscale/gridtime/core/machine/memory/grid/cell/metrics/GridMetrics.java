package com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics;

import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.MetricRowKey;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Duration;
import java.util.Map;


@NoArgsConstructor
@Getter
@Setter
@ToString
public class GridMetrics  {


    private Map<MetricRowKey, CandleStick> metricsByType = DefaultCollections.map();

    private GridMetrics cascadeToParent;

    public GridMetrics(GridMetrics cascadeToParent) {
        this.cascadeToParent = cascadeToParent;
    }

    private CandleStick findOrCreateCandle(MetricRowKey metricRowKey) {
        CandleStick candle = metricsByType.get(metricRowKey);
        if (candle == null) {
            candle = new CandleStick();
            metricsByType.put(metricRowKey, candle);
        }
        return candle;
    }

    public void addVelocitySample(Duration duration) {
        CandleStick candle = findOrCreateCandle(MetricRowKey.FILE_TRAVERSAL_VELOCITY);
        candle.addSample(duration.getSeconds());
        if (cascadeToParent != null) {
            cascadeToParent.addVelocitySample(duration);
        }
    }

    public void addModificationSample(int modificationCount) {
        CandleStick candle = findOrCreateCandle(MetricRowKey.MODIFICATION_COUNT);
        candle.addSample(modificationCount);
        if (cascadeToParent != null) {
            cascadeToParent.addModificationSample(modificationCount);
        }
    }

    public void addExecutionTimeSample(Duration executionTime) {
        CandleStick candle = findOrCreateCandle(MetricRowKey.EXECUTION_RUN_TIME);

        candle.addSample(executionTime.getSeconds());
        if (cascadeToParent != null) {
            cascadeToParent.addExecutionTimeSample(executionTime);
        }
    }

    public void addExecutionCycleTimeSample(Duration durationBetweenExecution) {
        CandleStick candle = findOrCreateCandle(MetricRowKey.EXECUTION_CYCLE_TIME);
        candle.addSample(durationBetweenExecution.getSeconds());
        if (cascadeToParent != null) {
            cascadeToParent.addExecutionCycleTimeSample(durationBetweenExecution);
        }
    }

    public void addFeelsSample(int feels) {
        CandleStick candle = findOrCreateCandle(MetricRowKey.FEELS);
        candle.addSample(feels);
        if (cascadeToParent != null) {
            cascadeToParent.addFeelsSample(feels);
        }
    }

    public void addWtfSample(boolean isWTF) {
        CandleStick candle = findOrCreateCandle(MetricRowKey.IS_WTF);

        if (isWTF) {
            candle.addSample(1);
        } else {
            candle.addSample(0);
        }
        if (cascadeToParent != null) {
            cascadeToParent.addWtfSample(isWTF);
        }
    }

    public void addLearningSample(boolean isLearning) {
        CandleStick candle = findOrCreateCandle(MetricRowKey.IS_LEARNING);
        if (isLearning) {
            candle.addSample(1);
        } else {
            candle.addSample(0);
        }
        if (cascadeToParent != null) {
            cascadeToParent.addLearningSample(isLearning);
        }
    }

    public void addPairingSample(boolean isPairing) {
        CandleStick candle = findOrCreateCandle(MetricRowKey.IS_PAIRING);

        if (isPairing) {
            candle.addSample(1);
        } else {
            candle.addSample(0);
        }
        if (cascadeToParent != null) {
            cascadeToParent.addPairingSample(isPairing);
        }
    }

    public void addFocusWeightSample(double focusWeight) {
        CandleStick candle = findOrCreateCandle(MetricRowKey.FOCUS_WEIGHT);

        candle.addSample(focusWeight);
        if (cascadeToParent != null) {
            cascadeToParent.addFocusWeightSample(focusWeight);
        }
    }

    @JsonIgnore
    public Duration getTotalTimeInvestment() {
        CandleStick velocityCandle = metricsByType.get(MetricRowKey.FILE_TRAVERSAL_VELOCITY);
        if (velocityCandle != null) {
            return Duration.ofSeconds(Math.round(velocityCandle.getTotal()));
        } else {
            return Duration.ofSeconds(0);
        }
    }

    public void resetMetrics() {
        metricsByType.clear();
    }


    public void combineWith(GridMetrics sourceMetrics) {
        combineAndSet(sourceMetrics, MetricRowKey.FILE_TRAVERSAL_VELOCITY);
        combineAndSet(sourceMetrics, MetricRowKey.MODIFICATION_COUNT);
        combineAndSet(sourceMetrics, MetricRowKey.FEELS);
        combineAndSet(sourceMetrics, MetricRowKey.IS_LEARNING);
        combineAndSet(sourceMetrics, MetricRowKey.IS_WTF);
        combineAndSet(sourceMetrics, MetricRowKey.IS_PAIRING);
        combineAndSet(sourceMetrics, MetricRowKey.EXECUTION_RUN_TIME);
        combineAndSet(sourceMetrics, MetricRowKey.EXECUTION_CYCLE_TIME);
        combineAndSet(sourceMetrics, MetricRowKey.FOCUS_WEIGHT);
    }

    private void combineAndSet(GridMetrics sourceMetrics, MetricRowKey metricRowKey) {
        CandleStick combinedCandle = combineCandles(getMetric(metricRowKey), sourceMetrics.getMetric(metricRowKey));
        if (combinedCandle != null) {
            metricsByType.put(metricRowKey, combinedCandle);
        }
    }

    public CandleStick getMetric(MetricRowKey metricRowKey) {
        return metricsByType.get(metricRowKey);
    }



    private CandleStick combineCandles(CandleStick destCandle, CandleStick candleToCombine) {
        if (destCandle == null && candleToCombine != null) {
            destCandle = new CandleStick();
        }
        if (candleToCombine != null) {
            destCandle.combineAggregate(candleToCombine);
        }

        return destCandle;
    }


}
