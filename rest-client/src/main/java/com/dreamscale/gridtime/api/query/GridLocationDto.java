package com.dreamscale.gridtime.api.query;

import com.dreamscale.gridtime.api.dictionary.WordDefinitionDto;
import com.dreamscale.gridtime.api.grid.GridTableResults;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GridLocationDto {

    String gridTime;
    String zoomLevel;
    Integer [] coordinates;

    GridTableResults gridTableResults;


}

