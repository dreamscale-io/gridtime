package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.admin.ProjectSyncInputDto;
import com.dreamscale.gridtime.api.admin.ProjectSyncOutputDto;
import com.dreamscale.gridtime.api.job.JobDescriptorDto;
import com.dreamscale.gridtime.api.organization.AutoConfigInputDto;
import com.dreamscale.gridtime.api.organization.MemberRegistrationDetailsDto;
import com.dreamscale.gridtime.core.service.JobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.JOB_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class JobResource {

    @Autowired
    private JobService jobService;


    @GetMapping
    List<JobDescriptorDto> getAllJobs() {
        return jobService.getAllTorchieJobs();
    }



}
