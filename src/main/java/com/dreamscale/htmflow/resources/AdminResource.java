package com.dreamscale.htmflow.resources;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.admin.ProjectSyncInputDto;
import com.dreamscale.htmflow.api.admin.ProjectSyncOutputDto;
import com.dreamscale.htmflow.core.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


}
