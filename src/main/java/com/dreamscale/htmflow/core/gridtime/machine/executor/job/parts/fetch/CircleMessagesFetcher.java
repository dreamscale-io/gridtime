package com.dreamscale.htmflow.core.gridtime.machine.executor.job.parts.fetch;

import com.dreamscale.htmflow.core.domain.circle.CircleFeedMessageEntity;
import com.dreamscale.htmflow.core.domain.circle.CircleFeedMessageRepository;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feed.Flowable;
import com.dreamscale.htmflow.core.gridtime.machine.executor.job.parts.fetch.flowable.FlowableCircleMessageEvent;
import com.dreamscale.htmflow.core.gridtime.machine.executor.job.parts.source.Bookmark;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class CircleMessagesFetcher extends FetchStrategy {

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

    private List<Flowable> convertToFlowables(List<CircleFeedMessageEntity> circleMessages) {
        List<Flowable> flowables = new ArrayList<>();

        for (CircleFeedMessageEntity circleMessage : circleMessages) {
            flowables.add(new FlowableCircleMessageEvent(circleMessage));
        }
        return flowables;
    }

}
