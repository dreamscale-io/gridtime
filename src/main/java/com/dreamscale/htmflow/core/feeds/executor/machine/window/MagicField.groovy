package com.dreamscale.htmflow.core.feeds.executor.machine.window

enum MagicField {

    COMPONENT("component"),
    PLACE("mainFocus")

    String fieldName

    MagicField(String fieldName) {
        this.fieldName = fieldName;
    }
}