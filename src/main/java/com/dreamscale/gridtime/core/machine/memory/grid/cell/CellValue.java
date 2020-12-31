package com.dreamscale.gridtime.core.machine.memory.grid.cell;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CellValue {
    String val;
    List<UUID> refs;
    List<String> glyphRefs;
}
