package com.dreamscale.gridtime.core.machine.executor.sketch.window

import com.dreamscale.gridtime.core.machine.executor.sketch.flow.Flow

class FlowWindow {

    BucketSize bucketSize
    List<MagicField> flowStreamKeys

    Map<MagicField, Flow<MagicValue>> flowStreamFieldMap

}
