package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.job.JobDescriptorDto;
import com.dreamscale.gridtime.core.service.GridtimeJobManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.JOB_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class JobResource {

    @Autowired
    private GridtimeJobManager gridtimeJobManager;


    List<JobDescriptorDto> getAllJobs() {
        return gridtimeJobManager.getAllTorchieJobs();
    }


    //so what I want to do, is be able to have a list of descriptive jobs

    //and be able to submit those jobs to Grid

    //then Grid manages a set of active jobs it's supposed to be running.

    //vision of it's template of what it ought to be doing.

    //GridTime Job Configuration Template

    //when a gridtime instance loads, it loads up it's job configuration.

    //you can get the job configuration, change the configuration, get the current status of the running jobs

    //tell a particular job to run.


}
