package com.dreamscale.gridtime;

import com.dreamscale.gridtime.core.service.JiraSyncService;
import com.dreamscale.gridtime.core.domain.journal.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class BootstrapInitializer {

    @Autowired
    private ProjectRepository repository;

    @Autowired
    JiraSyncService jiraSyncJob;

    @PostConstruct
    private void bootstrap() {


//        jiraSyncJob.synchonizeDBWithJira();
    }

}
