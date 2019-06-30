package com.dreamscale.htmflow.core.gridtime.machine.memory.grid.query.aggregate;

import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import lombok.Getter;

import java.util.Map;

public class PercentMetric {

    private Map<String, Counter> countByCategory = DefaultCollections.map();
    private Counter totalCount = new Counter();

    private Map<String, Double> percentByCategory = DefaultCollections.map();

    public void addSample(String category) {
       Counter counter = findOrCreateCategoryCounter(category);
       counter.increment();

       totalCount.increment();
    }

    public void calculatePercent(String category) {
        Counter categoryCounter = findOrCreateCategoryCounter(category);
        Double percent = 0.0;

        if (totalCount.getCount() > 0) {
            percent = (categoryCounter.getCount() * 1.0) / totalCount.getCount();
        }

        percentByCategory.put(category, percent);
    }

    private Counter findOrCreateCategoryCounter(String category) {
        Counter counter = countByCategory.get(category);
        if (counter == null) {
            counter = new Counter();
            countByCategory.put(category, counter);
        }
        return counter;
    }

    public Double getPercent(String category) {
        return percentByCategory.get(category);
    }

    @Getter
    private class Counter {
        int count;

        void increment() {
            count++;
        }
    }
}
