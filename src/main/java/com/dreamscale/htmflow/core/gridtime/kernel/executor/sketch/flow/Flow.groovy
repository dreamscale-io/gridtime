package com.dreamscale.htmflow.core.gridtime.kernel.executor.sketch.flow


import com.dreamscale.htmflow.core.gridtime.kernel.executor.sketch.window.MagicField
import com.dreamscale.htmflow.core.gridtime.kernel.clock.Metronome

import java.util.concurrent.ArrayBlockingQueue

class Flow<T> {

    Map<MagicField, ArrayBlockingQueue<T>> flowStreamMap;

    Metronome metronome
    MagicField focusField

    Flow(Metronome metronome) {
        this.metronome = metronome
        this.flowStreamMap = new HashMap<>();
    }

    void focus(MagicField field) {
        this.focusField = field;
    }

    //metronome, if time is past the frame, do we skip, fast forward?
    //maybe we consume feeds based on time sync, explore around, until committing to forwarding stream?
    //peaking should maybe allow for all things in queue, so you can go relative time
    // 1, 2, 3, 4, 5 (by window chunks?)?

    T see() {
        if (this.focusField == null) {
            throw new RuntimeException("Please focus before you see");
        }
        ArrayBlockingQueue<T> queue = flowStreamMap.get(this.focusField);
        return queue.take();
    }

    void fill(MagicField field, List<T> stuffToFlow) {
        ArrayBlockingQueue<T> queue = findOrCreateQueue(field);
        queue.addAll(stuffToFlow)
    }

    ArrayBlockingQueue<T> findOrCreateQueue(MagicField field) {
        ArrayBlockingQueue<T> queue = flowStreamMap.get(field);
        if (queue == null) {
            queue = new ArrayBlockingQueue<T>(20);
            flowStreamMap.put(field, queue);
        }
        return queue;
    }
}
