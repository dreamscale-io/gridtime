package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.flow.batch.NewFlowBatchDto;
import com.dreamscale.gridtime.api.flow.event.NewSnippetEventDto;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.service.LearningCircuitOperator;
import com.dreamscale.gridtime.core.service.FlowService;
import com.dreamscale.gridtime.core.service.OrganizationService;
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

    @Autowired
    OrganizationService organizationService;
    @Autowired
    LearningCircuitOperator learningCircuitOperator;


    /**
     * Saves a batch of flow activity and events from the IDE, (or another pluggable flow source)
     */

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.INPUT_PATH + ResourcePaths.BATCH_PATH)
    public void publishBatch(@RequestBody NewFlowBatchDto batch) {
        RequestContext context = RequestContext.get();
        log.info("publishBatch, user={}, batch={}", context.getRootAccountId(), batch);

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());

        flowService.saveFlowBatch(invokingMember.getOrganizationId(), invokingMember.getId(), batch);
    }

    /**
     * Publishes a snippet to the active Learning Circuit of the invoking member
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.INPUT_PATH + ResourcePaths.SNIPPET_PATH)
    public void publishSnippet(@RequestBody NewSnippetEventDto snippet) {
        RequestContext context = RequestContext.get();
        log.info("saveFlowSnippet, user={}, snippet={}", context.getRootAccountId(), snippet);

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());

        flowService.saveSnippetEvent(invokingMember.getOrganizationId(), invokingMember.getId(), snippet);

        learningCircuitOperator.publishSnippetToActiveCircuit(invokingMember.getOrganizationId(), invokingMember.getId(), snippet);

    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.AUTH_PING_PATH)
    public void authPing() {
    }

}
