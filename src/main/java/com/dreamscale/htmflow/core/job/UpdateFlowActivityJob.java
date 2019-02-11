package com.dreamscale.htmflow.core.job;

import com.dreamscale.htmflow.core.domain.flow.FlowActivityEntity;
import com.dreamscale.htmflow.core.domain.flow.FlowActivityRepository;
import com.dreamscale.htmflow.core.service.ComponentLookupService;
import org.springframework.stereotype.Component;

@Component
public class UpdateFlowActivityJob {

    FlowActivityRepository flowActivityRepository;

    ComponentLookupService componentLookupService;

    public void run() {
        Iterable<FlowActivityEntity> allActivity = flowActivityRepository.findAll();

        for (FlowActivityEntity flowActivityEntity : allActivity) {

            String filePath = flowActivityEntity.getMetadataValue("filePath");
            String component = componentLookupService.lookupDefaultComponent(filePath);

            if (flowActivityEntity.getComponent() == null) {
                flowActivityEntity.setComponent(component);
                flowActivityRepository.save(flowActivityEntity);
            }
        }
    }
}
