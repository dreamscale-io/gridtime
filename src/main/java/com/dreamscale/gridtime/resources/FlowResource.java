package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.flow.batch.NewFlowBatchDto;
import com.dreamscale.gridtime.api.flow.event.NewSnippetEventDto;
import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.service.FlowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.FLOW_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class FlowResource {

    @Autowired
    FlowService flowService;


    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.INPUT_PATH + ResourcePaths.BATCH_PATH)
    public void saveInputFlowBatch(@RequestBody NewFlowBatchDto batch) {
        saveFlowBatch(batch);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.INPUT_PATH + ResourcePaths.SNIPPET_PATH)
    public void saveInputFlowSnippet(@RequestBody NewSnippetEventDto snippet) {
        saveFlowSnippet(snippet);
    }

    /**
     * Saves a batch of flow activity and events for the user
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.BATCH_PATH)
    public void saveFlowBatch(@RequestBody NewFlowBatchDto batch) {
        RequestContext context = RequestContext.get();
        log.info("addFlowBatch, user={}, batch={}", context.getMasterAccountId(), batch);
        flowService.saveFlowBatch(context.getMasterAccountId(), batch);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.SNIPPET_PATH)
    public void saveFlowSnippet(@RequestBody NewSnippetEventDto snippet) {
        RequestContext context = RequestContext.get();
        log.info("saveFlowSnippet, user={}, snippet={}", context.getMasterAccountId(), snippet);
        flowService.saveSnippetEvent(context.getMasterAccountId(), snippet);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.AUTH_PING_PATH)
    public void authPing() {
    }

}
