package com.dreamscale.gridtime.core.machine.memory.feature.details;

import com.dreamscale.gridtime.api.grid.Observable;
import com.dreamscale.gridtime.core.machine.memory.cache.SearchKeyGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FeelsRatingDetails implements FeatureDetails, Observable {

    private Integer rating;

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
