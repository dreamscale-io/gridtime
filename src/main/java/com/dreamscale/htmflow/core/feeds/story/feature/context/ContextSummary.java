package com.dreamscale.htmflow.core.feeds.story.feature.context;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContextSummary implements FlowFeature {

    private ContextBeginningEvent projectContext;
    private ContextBeginningEvent taskContext;
    private ContextBeginningEvent intentionContext;

}
