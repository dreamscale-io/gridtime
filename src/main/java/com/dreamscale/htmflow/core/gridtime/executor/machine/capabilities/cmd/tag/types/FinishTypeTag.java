package com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.types;

import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.FinishTag;

public enum  FinishTypeTag implements FinishTag {
    Success,
    Fail,
    Abort,
    FirstGreenAfterRed;

}
