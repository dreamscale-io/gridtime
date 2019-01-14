package com.dreamscale.htmflow.core.torchie.machine.flow

import com.dreamscale.htmflow.core.torchie.Metronome
import com.dreamscale.htmflow.core.torchie.machine.window.MagicField

import java.util.concurrent.ArrayBlockingQueue

class Flow<T> {

    Map<MagicField, ArrayBlockingQueue<T>> flowStreamMap;

    MagicField focusField

    Flow() {
        this.flowStreamMap = new HashMap<>();
    }

    void focus(MagicField field) {
        this.focusField = field;
    }

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
