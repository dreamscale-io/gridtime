package com.dreamscale.htmflow;

import com.dreamscale.htmflow.core.mapping.CPGBucketConfig;
import com.dreamscale.htmflow.core.mapping.ProjectBuckets;
import com.dreamscale.htmflow.core.service.ComponentLookupService;
import com.dreamscale.htmflow.core.service.JiraSyncService;
import com.dreamscale.htmflow.core.domain.journal.ProjectRepository;
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
