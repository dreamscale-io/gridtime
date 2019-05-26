package com.dreamscale.htmflow.core.feeds.executor.parts.observer;

import com.dreamscale.htmflow.core.feeds.common.Flowable;
import com.dreamscale.htmflow.core.feeds.story.TileBuilder;

import java.lang.reflect.ParameterizedType;
import java.util.List;

public interface FlowObserver<T extends Flowable> {

    void seeInto(List<T> flowables, TileBuilder tileBuilder);

    default Class<T> getFlowableType() {
        return (Class<T>) ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }
}
