package com.dreamscale.htmflow.core.gridtime.executor.memory.feature.details;

import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.returns.Observable;
import com.dreamscale.htmflow.core.gridtime.executor.memory.search.SearchKeyGenerator;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeelsRatingDetails implements FeatureDetails, Observable {

    private Integer rating;

    public FeelsRatingDetails(int rating) {
        this.rating = rating;
    }

    public String toSearchKey() {
       return SearchKeyGenerator.createFeelsSearchKey(rating);
    }


    public String toString() {
        return toSearchKey();
    }

    @Override
    public String toDisplayString() {
        return Integer.toString(rating);
    }
}
