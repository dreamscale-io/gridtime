package com.dreamscale.gridtime.core.machine.memory.tag.types;

import com.dreamscale.gridtime.core.machine.memory.tag.FinishTag;

public enum  FinishTypeTag implements FinishTag {
    Success,
    Fail,
    Abort,
    FirstGreenAfterRed, DoItLater;

}
