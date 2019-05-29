package com.dreamscale.htmflow.core.gridtime.executor.sketch.window

import com.dreamscale.htmflow.core.gridtime.executor.sketch.flow.Flow

class FlowWindow {

    BucketSize bucketSize
    List<MagicField> flowStreamKeys

    Map<MagicField, Flow<MagicValue>> flowStreamFieldMap

}
