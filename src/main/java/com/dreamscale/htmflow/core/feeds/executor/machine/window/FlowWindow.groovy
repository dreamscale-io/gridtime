package com.dreamscale.htmflow.core.feeds.executor.machine.window

import com.dreamscale.htmflow.core.feeds.executor.machine.flow.Flow

class FlowWindow {

    BucketSize bucketSize
    List<MagicField> flowStreamKeys

    Map<MagicField, Flow<MagicValue>> flowStreamFieldMap

}
