package com.dreamscale.gridtime.api.project;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BoxMatcherConfigDto {
    String box;
    String include;
    List<String> excludeList;

    public BoxMatcherConfigDto(String box, String include) {
        this.box = box;
        this.include = include;
    }
}
