package com.dreamscale.gridtime.core.machine.memory.grid.cell;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Set;

@Getter
@Setter
public class CellValueMap {

    private LinkedHashMap<String, CellValue> rowValues = new LinkedHashMap<>();

    public void put(String beat, CellValue cellValue) {
        rowValues.put(beat, cellValue);
    }

    @JsonIgnore
    public CellValue get(String key) {
        return rowValues.get(key);
    }

    @JsonIgnore
    public int size() {
        return rowValues.size();
    }

    @JsonIgnore
    public String getFirstKey() {
        String firstKey = null;
        if (rowValues.size() > 0) {
            firstKey = rowValues.keySet().iterator().next();
        }
        return firstKey;
    }

    public Set<String> keySet() {
        return rowValues.keySet();
    }


}
