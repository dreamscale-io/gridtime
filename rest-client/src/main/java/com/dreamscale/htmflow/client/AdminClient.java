package com.dreamscale.htmflow.client;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.admin.ProjectSyncInputDto;
import com.dreamscale.htmflow.api.admin.ProjectSyncOutputDto;
import feign.Headers;
import feign.RequestLine;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface AdminClient {

    @RequestLine("POST " + ResourcePaths.ADMIN_PATH + ResourcePaths.CONFIG_PATH + ResourcePaths.SYNC_PATH)
    ProjectSyncOutputDto configProjectSync(ProjectSyncInputDto projectSyncDto);

}
