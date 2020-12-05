package com.dreamscale.gridtime.api.query;

import com.dreamscale.gridtime.api.grid.GridTableResults;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationInputDto {

    String locationExpression;
}

