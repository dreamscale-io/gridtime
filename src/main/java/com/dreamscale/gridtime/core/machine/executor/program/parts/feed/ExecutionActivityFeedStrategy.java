package com.dreamscale.gridtime.core.machine.executor.program.parts.feed;

import com.dreamscale.gridtime.core.domain.flow.FlowActivityEntity;
import com.dreamscale.gridtime.core.domain.flow.FlowActivityRepository;
import com.dreamscale.gridtime.core.machine.memory.feed.Flowable;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable.FlowableFlowActivity;
import com.dreamscale.gridtime.core.machine.executor.program.parts.source.Bookmark;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class ExecutionActivityFeedStrategy extends FeedStrategy {

    @Autowired
    FlowActivityRepository flowActivityRepository;

    @Override
    public Batch fetchNextBatch(UUID memberId, Bookmark bookmark, int fetchSize) {

        LocalDateTime afterDate = bookmark.getPosition();
        Long sequence = bookmark.getSequenceNumber();

        List<FlowActivityEntity> flowActivityEntities =
                flowActivityRepository.findExecutionActivityByMemberIdAfterDateWithLimit(memberId, Timestamp.valueOf(afterDate), sequence, fetchSize);

        List<Flowable> flowables = convertToFlowables(flowActivityEntities);

        return new Batch(memberId, bookmark, flowables);

    }

    @Override
    public FeedStrategyFactory.FeedType getFeedType() {
        return FeedStrategyFactory.FeedType.EXECUTION_ACTIVITY_FEED;
    }


    private List<Flowable> convertToFlowables(List<FlowActivityEntity> flowActivityEntities) {
        List<Flowable> flowables = new ArrayList<>();

        for (FlowActivityEntity flowActivity : flowActivityEntities) {
            flowables.add(new FlowableFlowActivity(flowActivity));
        }
        return flowables;
    }



}
