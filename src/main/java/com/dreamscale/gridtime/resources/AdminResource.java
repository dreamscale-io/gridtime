package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.admin.ProjectSyncInputDto;
import com.dreamscale.gridtime.api.admin.ProjectSyncOutputDto;
import com.dreamscale.gridtime.api.organization.AutoConfigInputDto;
import com.dreamscale.gridtime.api.organization.MemberRegistrationDetailsDto;
import com.dreamscale.gridtime.api.status.PingDto;
import com.dreamscale.gridtime.core.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

    @PostMapping(ResourcePaths.PING_PATH)
    PingDto ping() {
        log.info("ping!");

        LocalDateTime now = LocalDateTime.now();
        ZonedDateTime nowZone = now.atZone(ZoneId.of("UTC"));

        return new PingDto(LocalDateTime.now(), nowZone);
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
     * Configure the hard coded bucket configuration in associate with the team
     */
    @PostMapping(ResourcePaths.CONFIG_PATH + ResourcePaths.ORG_PATH + ResourcePaths.ONPREM_PATH + ResourcePaths.BUCKET_PATH)
    void configureOnPremBuckets() {

        adminService.configureOnPremBuckets();
    }


}
