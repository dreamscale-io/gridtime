package com.dreamscale.ideaflow.core.feeds.executor.machine.window

import com.dreamscale.ideaflow.core.feeds.executor.machine.flow.Flow

class FlowWindow {

    BucketSize bucketSize
    List<MagicField> flowStreamKeys

    Map<MagicField, Flow<MagicValue>> flowStreamFieldMap

}
