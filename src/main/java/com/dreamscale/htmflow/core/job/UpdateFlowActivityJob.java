package com.dreamscale.htmflow.core.job;

import com.dreamscale.htmflow.core.domain.flow.FlowActivityEntity;
import com.dreamscale.htmflow.core.domain.flow.FlowActivityRepository;
import com.dreamscale.htmflow.core.service.ComponentLookupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class UpdateFlowActivityJob {

    @Autowired
    FlowActivityRepository flowActivityRepository;

    @Autowired
    ComponentLookupService componentLookupService;

    public void run() {

        PageRequest firstPageRequest = new PageRequest(0, 1000);
        Page<FlowActivityEntity> firstPage = flowActivityRepository.findAll(firstPageRequest);

        int totalPages = firstPage.getTotalPages();

        for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
            PageRequest pageRequest = new PageRequest(pageIndex, 1000);

            Page<FlowActivityEntity> currentPage = flowActivityRepository.findAll(pageRequest);

            for (FlowActivityEntity flowActivityEntity : currentPage) {

                String filePath = flowActivityEntity.getMetadataValue("filePath");
                String component = componentLookupService.lookupDefaultComponent(filePath);

                if (flowActivityEntity.getComponent() == null) {
                    flowActivityEntity.setComponent(component);
                    flowActivityRepository.save(flowActivityEntity);
                }
            }
        }

    }
}
