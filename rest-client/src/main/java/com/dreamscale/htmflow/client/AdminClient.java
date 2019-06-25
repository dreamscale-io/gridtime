package com.dreamscale.htmflow.client;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.admin.ProjectSyncInputDto;
import com.dreamscale.htmflow.api.admin.ProjectSyncOutputDto;
import com.dreamscale.htmflow.api.organization.AutoConfigInputDto;
import com.dreamscale.htmflow.api.organization.MemberRegistrationDetailsDto;
import feign.Headers;
import feign.RequestLine;

import java.util.List;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface AdminClient {

    @RequestLine("POST " + ResourcePaths.ADMIN_PATH + ResourcePaths.CONFIG_PATH + ResourcePaths.SYNC_PATH)
    ProjectSyncOutputDto configProjectSync(ProjectSyncInputDto projectSyncDto);

    @RequestLine("POST " + ResourcePaths.ADMIN_PATH + ResourcePaths.JIRA_PATH + ResourcePaths.SYNC_PATH)
    void syncAllOrgs();

    @RequestLine("POST " + ResourcePaths.ADMIN_PATH + ResourcePaths.CONFIG_PATH + ResourcePaths.ORG_PATH + ResourcePaths.DREAMSCALE_PATH)
    List<MemberRegistrationDetailsDto> configureDreamScaleOrg(AutoConfigInputDto inputConfig);

    @RequestLine("POST " + ResourcePaths.ADMIN_PATH + ResourcePaths.CONFIG_PATH + ResourcePaths.ORG_PATH + ResourcePaths.ONPREM_PATH)
    List<MemberRegistrationDetailsDto> configureOnPremOrg(AutoConfigInputDto inputConfig);


    @RequestLine("POST " + ResourcePaths.ADMIN_PATH + ResourcePaths.CONFIG_PATH + ResourcePaths.ORG_PATH + ResourcePaths.ONPREM_PATH + ResourcePaths.BUCKET_PATH)
    void configureOnPremBuckets();

}
