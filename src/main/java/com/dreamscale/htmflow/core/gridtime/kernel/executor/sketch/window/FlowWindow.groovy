package com.dreamscale.htmflow.core.gridtime.kernel.executor.sketch.window

import com.dreamscale.htmflow.core.gridtime.kernel.executor.sketch.flow.Flow

class FlowWindow {

    BucketSize bucketSize
    List<MagicField> flowStreamKeys

    Map<MagicField, Flow<MagicValue>> flowStreamFieldMap

}
