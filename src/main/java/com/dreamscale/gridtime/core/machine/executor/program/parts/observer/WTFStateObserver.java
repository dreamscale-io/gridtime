package com.dreamscale.gridtime.core.machine.executor.program.parts.observer;

import com.dreamscale.gridtime.core.domain.circuit.message.WTFFeedMessageEntity;
import com.dreamscale.gridtime.core.hooks.talk.dto.TalkMessageType;
import com.dreamscale.gridtime.core.machine.memory.tag.types.FinishTypeTag;
import com.dreamscale.gridtime.core.machine.memory.tag.types.StartTypeTag;
import com.dreamscale.gridtime.core.machine.executor.program.parts.source.Window;
import com.dreamscale.gridtime.core.machine.memory.feature.details.CircuitDetails;
import com.dreamscale.gridtime.core.machine.memory.feed.Flowable;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable.FlowableCircuitWTFMessageEvent;
import com.dreamscale.gridtime.core.machine.memory.tile.GridTile;

/**
 * Translates the Circle Feed messages of start/stop shelf/resume on WTF Circles to TimeBands
 */
public class WTFStateObserver implements FlowObserver<FlowableCircuitWTFMessageEvent> {

    @Override
    public void see(Window<FlowableCircuitWTFMessageEvent> window, GridTile gridTile) {

        for (Flowable flowable : window.getFlowables()) {
            WTFFeedMessageEntity feedMessageEntity = (flowable.get());

            TalkMessageType messageType = feedMessageEntity.getMessageType();

            if (isCircuitBeginningEvent(messageType)) {
                gridTile.startWTF(feedMessageEntity.getPosition(), createCircuitDetails(feedMessageEntity), decodeStartTag(feedMessageEntity.getMessageType()));
            }

            if (isCircleEndingEvent(messageType)) {
                gridTile.clearWTF(feedMessageEntity.getPosition(), decodeFinishTag(feedMessageEntity.getMessageType()));
            }
        }
    }

    private CircuitDetails createCircuitDetails(WTFFeedMessageEntity messageEntity) {
        return new CircuitDetails(messageEntity.getCircuitId(), messageEntity.getCircuitName());
    }


    private StartTypeTag decodeStartTag(TalkMessageType messageType) {
        if (messageType.equals(TalkMessageType.CIRCUIT_OPEN)) {
            return StartTypeTag.Start;
        } else if (messageType.equals(TalkMessageType.CIRCUIT_RESUMED)) {
            return StartTypeTag.Resume;
        }
        return StartTypeTag.Start;
    }

    private FinishTypeTag decodeFinishTag(TalkMessageType messageType) {
        if (messageType.equals(TalkMessageType.CIRCUIT_CLOSED)) {
            return FinishTypeTag.Success;
        } else if (messageType.equals(TalkMessageType.CIRCUIT_ONHOLD)) {
            return FinishTypeTag.DoItLater;
        }
        return FinishTypeTag.Success;
    }


    private boolean isCircuitBeginningEvent(TalkMessageType messageType) {
        return messageType.equals(TalkMessageType.CIRCUIT_OPEN)
                || messageType.equals(TalkMessageType.CIRCUIT_RESUMED);
    }

    private boolean isCircleEndingEvent(TalkMessageType messageType) {
        return messageType.equals(TalkMessageType.CIRCUIT_CLOSED)
                || messageType.equals(TalkMessageType.CIRCUIT_ONHOLD);
    }
}
