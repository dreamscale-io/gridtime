package com.dreamscale.htmflow.resources;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.admin.ProjectSyncInputDto;
import com.dreamscale.htmflow.api.admin.ProjectSyncOutputDto;
import com.dreamscale.htmflow.api.organization.AutoConfigInputDto;
import com.dreamscale.htmflow.api.organization.MemberRegistrationDetailsDto;
import com.dreamscale.htmflow.core.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.ADMIN_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class AdminResource {

    @Autowired
    private AdminService adminService;

    /**
     * Configure a Jira Project for Synchronization
     */
    @PostMapping(ResourcePaths.CONFIG_PATH + ResourcePaths.SYNC_PATH)
    ProjectSyncOutputDto configureJiraProjectSynchronization(@RequestBody ProjectSyncInputDto projectSyncDto) {

        return adminService.configureJiraProjectSync(projectSyncDto);
    }


    /**
     * Synchronize all Jira Projects for all organizations
     */
    @PostMapping(ResourcePaths.JOB_PATH + ResourcePaths.UPDATE_PATH + ResourcePaths.ACTIVITY_PATH)
    void runJobToUpdateFlowActivityComponents() {
        adminService.runJobToUpdateFlowActivityComponents();
    }


    /**
     * Synchronize all Jira Projects for all organizations
     */
    @PostMapping(ResourcePaths.JIRA_PATH + ResourcePaths.SYNC_PATH)
    void synchronizeWithJira() {
        adminService.synchronizeAllOrgs();
    }

    /**
     * Configure DreamScale Organization with all membership accounts ready for activation
     */
    @PostMapping(ResourcePaths.CONFIG_PATH + ResourcePaths.ORG_PATH + ResourcePaths.DREAMSCALE_PATH)
    List<MemberRegistrationDetailsDto> configureDreamScaleOrg(@RequestBody AutoConfigInputDto inputConfig) {
        return adminService.configureDreamScale(inputConfig);
    }

    /**
     * Configure OnPrem Organization with all membership accounts ready for activation
     */
    @PostMapping(ResourcePaths.CONFIG_PATH + ResourcePaths.ORG_PATH + ResourcePaths.ONPREM_PATH)
    List<MemberRegistrationDetailsDto>  configureOnPremOrg(@RequestBody AutoConfigInputDto inputConfig) {
        return adminService.configureOnPrem(inputConfig);
    }


    /**
     * Configure OnPrem component buckets so that all Idea Flow data gets dereferenced into components
     * This is memory resident, not persistent, but maybe should be persistent too?  Would mayb
     */
    @PostMapping(ResourcePaths.CONFIG_PATH + ResourcePaths.BUCKET_PATH + ResourcePaths.ONPREM_PATH)
    List<MemberRegistrationDetailsDto>  configureOnPremBuckets(@RequestBody AutoConfigInputDto inputConfig) {

        //TODO make this configure the buckets

        return adminService.configureOnPrem(inputConfig);
    }


}
