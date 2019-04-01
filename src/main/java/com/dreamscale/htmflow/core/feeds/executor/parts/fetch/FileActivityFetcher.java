package com.dreamscale.htmflow.core.feeds.executor.parts.fetch;

import com.dreamscale.htmflow.core.domain.flow.FlowActivityEntity;
import com.dreamscale.htmflow.core.domain.flow.FlowActivityRepository;
import com.dreamscale.htmflow.core.feeds.common.Flowable;
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.flowable.FlowableFlowActivity;
import com.dreamscale.htmflow.core.feeds.executor.parts.source.Bookmark;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class FileActivityFetcher extends FetchStrategy {

    @Autowired
    FlowActivityRepository flowActivityRepository;

    @Override
    public Batch fetchNextBatch(UUID memberId, Bookmark bookmark, int fetchSize) {

        LocalDateTime afterDate = bookmark.getPosition();
        Long sequence = bookmark.getSequenceNumber();

        List<FlowActivityEntity> flowActivityEntities =
                flowActivityRepository.findFileActivityByMemberIdAfterDateWithLimit(memberId, Timestamp.valueOf(afterDate), sequence, fetchSize);

        List<Flowable> flowables = convertToFlowables(flowActivityEntities);

        return new Batch(memberId, bookmark, flowables);

    }


    private List<Flowable> convertToFlowables(List<FlowActivityEntity> flowActivityEntities) {
        List<Flowable> flowables = new ArrayList<>();

        for (FlowActivityEntity flowActivity : flowActivityEntities) {
            flowables.add(new FlowableFlowActivity(flowActivity));
        }
        return flowables;
    }



}
