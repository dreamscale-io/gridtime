package com.dreamscale.htmflow.core.gridtime.executor.machine.parts.fetch.flowable;

import com.dreamscale.htmflow.core.domain.flow.FlowActivityEntity;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feed.FlowableActivity;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.source.Bookmark;

import java.time.LocalDateTime;

public class FlowableFlowActivity extends FlowableActivity {

    private final FlowActivityEntity flowActivity;

    public FlowableFlowActivity(FlowActivityEntity flowActivityEntity) {
        this.flowActivity = flowActivityEntity;
    }

    @Override
    public Bookmark getBookmark() {
        return new Bookmark(flowActivity.getStart(), flowActivity.getId());
    }

    @Override
    public <T> T get() {
        return (T)flowActivity;
    }

    @Override
    public LocalDateTime getStart() {
        return flowActivity.getStart();
    }

    @Override
    public LocalDateTime getEnd() {
        return flowActivity.getEnd();
    }

    @Override
    public void setStart(LocalDateTime newStart) {
        flowActivity.setStart(newStart);
    }

    @Override
    public void setEnd(LocalDateTime newEnd) {
        flowActivity.setEnd(newEnd);
    }

    @Override
    public FlowableActivity cloneActivity() throws CloneNotSupportedException {
        FlowActivityEntity clone = (FlowActivityEntity) flowActivity.clone();

        return new FlowableFlowActivity(clone);
    }

    @Override
    public String toDisplayString() {
        return "FlowActivity["+getStart()+"]";
    }

}
