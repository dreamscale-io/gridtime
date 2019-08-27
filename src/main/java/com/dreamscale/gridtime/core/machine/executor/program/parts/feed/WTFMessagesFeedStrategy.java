package com.dreamscale.gridtime.core.machine.executor.program.parts.feed;

import com.dreamscale.gridtime.core.domain.circle.CircleFeedMessageEntity;
import com.dreamscale.gridtime.core.domain.circle.CircleFeedMessageRepository;
import com.dreamscale.gridtime.core.machine.memory.feed.Flowable;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable.FlowableCircleMessageEvent;
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
    CircleFeedMessageRepository circleFeedMessageRepository;


    @Override
    public Batch fetchNextBatch(UUID memberId, Bookmark bookmark, int fetchSize) {

        LocalDateTime afterDate = bookmark.getPosition();

        List<CircleFeedMessageEntity> circleMessageEntities =
                circleFeedMessageRepository.findByOwnerIdAfterDateWithLimit(memberId, Timestamp.valueOf(afterDate), fetchSize);

        List<Flowable> flowables = convertToFlowables(circleMessageEntities);

        return new Batch(memberId, bookmark, flowables);

    }

    @Override
    public FeedStrategyFactory.FeedType getFeedType() {
        return FeedStrategyFactory.FeedType.WTF_MESSAGES_FEED;
    }

    private List<Flowable> convertToFlowables(List<CircleFeedMessageEntity> circleMessages) {
        List<Flowable> flowables = new ArrayList<>();

        for (CircleFeedMessageEntity circleMessage : circleMessages) {
            flowables.add(new FlowableCircleMessageEvent(circleMessage));
        }
        return flowables;
    }

}
