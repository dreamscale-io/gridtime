package com.dreamscale.htmflow.resources;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.batch.NewIFMBatch;
import com.dreamscale.htmflow.core.domain.OrganizationMemberEntity;
import com.dreamscale.htmflow.core.security.RequestContext;
import com.dreamscale.htmflow.core.service.FlowService;
import com.dreamscale.htmflow.core.service.OrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.FLOW_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class FlowResource {

    @Autowired
    FlowService flowService;

    /**
     * Saves a batch of flow activity and events for the user
     */
    @PostMapping(ResourcePaths.BATCH_PATH)
    public void saveFlowBatch(@RequestBody NewIFMBatch batch) {
        log.info("addFlowBatch, batch={}", batch);

        RequestContext context = RequestContext.get();

        flowService.saveFlowBatch(context.getMasterAccountId(), batch);
    }

}
