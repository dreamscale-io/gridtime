package com.dreamscale.gridtime.core.machine.executor.program.parts.feed;

import com.dreamscale.gridtime.core.domain.circuit.message.WTFFeedMessageEntity;
import com.dreamscale.gridtime.core.domain.circuit.message.WTFFeedMessageRepository;
import com.dreamscale.gridtime.core.machine.memory.feed.Flowable;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable.FlowableCircuitWTFMessageEvent;
import com.dreamscale.gridtime.core.machine.executor.program.parts.source.Bookmark;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class WTFMessagesFeedStrategy extends FeedStrategy {

    @Autowired
    WTFFeedMessageRepository wtfFeedMessageRepository;


    @Override
    public Batch fetchNextBatch(UUID memberId, Bookmark bookmark, int fetchSize) {

        LocalDateTime afterDate = bookmark.getPosition();

        List<WTFFeedMessageEntity> wtfFeedMessages =
                wtfFeedMessageRepository.findByOwnerIdAfterDateWithLimit(memberId, Timestamp.valueOf(afterDate), fetchSize);

        List<Flowable> flowables = convertToFlowables(wtfFeedMessages);

        return new Batch(memberId, bookmark, flowables);

    }

    @Override
    public FeedStrategyFactory.FeedType getFeedType() {
        return FeedStrategyFactory.FeedType.WTF_MESSAGES_FEED;
    }

    private List<Flowable> convertToFlowables(List<WTFFeedMessageEntity> circuitMessages) {
        List<Flowable> flowables = new ArrayList<>();

        for (WTFFeedMessageEntity circuitMessage : circuitMessages) {
            flowables.add(new FlowableCircuitWTFMessageEvent(circuitMessage));
        }
        return flowables;
    }

}
