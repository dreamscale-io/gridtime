package com.dreamscale.gridtime.core.machine.executor.sketch.window

enum MagicField {

    COMPONENT("component"),
    PLACE("mainFocus")

    String fieldName

    MagicField(String fieldName) {
        this.fieldName = fieldName;
    }
}