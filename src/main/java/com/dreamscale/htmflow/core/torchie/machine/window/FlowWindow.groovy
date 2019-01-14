package com.dreamscale.htmflow.core.torchie.machine.window

import com.dreamscale.htmflow.core.torchie.machine.flow.Flow

class FlowWindow {

    BucketSize bucketSize
    List<MagicField> flowStreamKeys

    Map<MagicField, Flow<MagicValue>> flowStreamFieldMap

}
