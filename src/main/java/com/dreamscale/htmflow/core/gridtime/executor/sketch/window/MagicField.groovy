package com.dreamscale.htmflow.core.gridtime.executor.sketch.window

enum MagicField {

    COMPONENT("component"),
    PLACE("mainFocus")

    String fieldName

    MagicField(String fieldName) {
        this.fieldName = fieldName;
    }
}