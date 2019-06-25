package com.dreamscale.htmflow.core.gridtime.kernel.memory.tag.types;

import com.dreamscale.htmflow.core.gridtime.kernel.memory.tag.FinishTag;

public enum  FinishTypeTag implements FinishTag {
    Success,
    Fail,
    Abort,
    FirstGreenAfterRed, DoItLater;

}
