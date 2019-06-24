package com.dreamscale.htmflow.core.gridtime.machine.executor.sketch.window

import com.dreamscale.htmflow.core.gridtime.machine.executor.sketch.flow.Flow

class FlowWindow {

    BucketSize bucketSize
    List<MagicField> flowStreamKeys

    Map<MagicField, Flow<MagicValue>> flowStreamFieldMap

}
