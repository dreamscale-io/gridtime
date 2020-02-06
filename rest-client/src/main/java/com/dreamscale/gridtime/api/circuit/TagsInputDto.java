package com.dreamscale.gridtime.api.circuit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagsInputDto {

    private List<String> tags;

    public TagsInputDto(String ... tags) {
        this.tags = Arrays.asList(tags);
    }

}
