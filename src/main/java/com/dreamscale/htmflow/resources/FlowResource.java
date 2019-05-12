package com.dreamscale.htmflow.resources;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.batch.NewFlowBatch;
import com.dreamscale.htmflow.api.event.NewSnippetEvent;
import com.dreamscale.htmflow.core.security.RequestContext;
import com.dreamscale.htmflow.core.service.FlowService;
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
    public void saveInputFlowBatch(@RequestBody NewFlowBatch batch) {
        saveFlowBatch(batch);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.INPUT_PATH + ResourcePaths.SNIPPET_PATH)
    public void saveInputFlowSnippet(@RequestBody  NewSnippetEvent snippet) {
        saveFlowSnippet(snippet);
    }

    /**
     * Saves a batch of flow activity and events for the user
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.BATCH_PATH)
    public void saveFlowBatch(@RequestBody NewFlowBatch batch) {
        RequestContext context = RequestContext.get();
        log.info("addFlowBatch, user={}, batch={}", context.getMasterAccountId(), batch);
        flowService.saveFlowBatch(context.getMasterAccountId(), batch);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.SNIPPET_PATH)
    public void saveFlowSnippet(@RequestBody NewSnippetEvent snippet) {
        RequestContext context = RequestContext.get();
        log.info("saveFlowSnippet, user={}, snippet={}", context.getMasterAccountId(), snippet);
        flowService.saveSnippetEvent(context.getMasterAccountId(), snippet);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.AUTH_PING_PATH)
    public void authPing() {
    }

}
