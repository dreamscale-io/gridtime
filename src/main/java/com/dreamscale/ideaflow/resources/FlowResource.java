package com.dreamscale.ideaflow.resources;

import com.dreamscale.ideaflow.api.ResourcePaths;
import com.dreamscale.ideaflow.api.batch.NewFlowBatch;
import com.dreamscale.ideaflow.api.event.NewSnippetEvent;
import com.dreamscale.ideaflow.core.security.RequestContext;
import com.dreamscale.ideaflow.core.service.FlowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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
