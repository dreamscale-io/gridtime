package com.dreamscale.htmflow.core.gridtime.machine.memory.tag.types;

import com.dreamscale.htmflow.core.gridtime.machine.memory.tag.FinishTag;

public enum  FinishTypeTag implements FinishTag {
    Success,
    Fail,
    Abort,
    FirstGreenAfterRed, DoItLater;

}
