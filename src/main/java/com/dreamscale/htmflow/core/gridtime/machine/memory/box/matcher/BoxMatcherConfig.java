package com.dreamscale.htmflow.core.gridtime.machine.memory.box.matcher;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BoxMatcherConfig {
    String box;
    String include;
    List<String> excludeList;

    public BoxMatcherConfig(String box, String include) {
        this.box = box;
        this.include = include;
    }
}
