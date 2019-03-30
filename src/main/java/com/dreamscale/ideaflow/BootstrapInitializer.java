package com.dreamscale.ideaflow;

import com.dreamscale.ideaflow.core.mapping.CPGBucketConfig;
import com.dreamscale.ideaflow.core.mapping.ProjectBuckets;
import com.dreamscale.ideaflow.core.service.ComponentLookupService;
import com.dreamscale.ideaflow.core.service.JiraSyncService;
import com.dreamscale.ideaflow.core.domain.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class BootstrapInitializer {

    @Autowired
    private ProjectRepository repository;

    @Autowired
    JiraSyncService jiraSyncJob;

    @Autowired
    ComponentLookupService componentLookupService;

    @PostConstruct
    private void bootstrap() {

        ProjectBuckets defaultBuckets = new CPGBucketConfig().createBuckets();
        componentLookupService.configureDefaultBuckets(defaultBuckets);
//        jiraSyncJob.synchonizeDBWithJira();
    }

}
