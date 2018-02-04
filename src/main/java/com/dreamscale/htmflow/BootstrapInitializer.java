package com.dreamscale.htmflow;

import com.dreamscale.htmflow.core.domain.ProjectEntity;
import com.dreamscale.htmflow.core.domain.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Component
public class BootstrapInitializer {

    @Autowired
    private ProjectRepository repository;

    @PostConstruct
    private void bootstrap() {
        repository.save(
                ProjectEntity.builder()
                .id(UUID.randomUUID())
                .name("Test Project")
                .externalId("12345")
                .build()
        );
    }

}
