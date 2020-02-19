package com.dreamscale.gridtime.api.dictionary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WordDefinitionInputDto {

    private String wordName;
    private String definition;
}
