package com.dreamscale.htmflow.api.journal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FlameRatingInputDto {

    private int flameRating; //flame rating from -5 to 5

    public boolean isValid() {
        return flameRating >= -5 && flameRating <= 5;
    }
}
