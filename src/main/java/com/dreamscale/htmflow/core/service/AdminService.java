package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.admin.ProjectSyncInputDto;
import com.dreamscale.htmflow.api.admin.ProjectSyncOutputDto;
import com.dreamscale.htmflow.core.domain.ConfigProjectSyncEntity;
import com.dreamscale.htmflow.core.domain.ConfigProjectSyncRepository;
import com.dreamscale.htmflow.core.mapper.DtoEntityMapper;
import com.dreamscale.htmflow.core.mapper.MapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Slf4j
@Service
public class AdminService {

    @Autowired
    ConfigProjectSyncRepository configProjectSyncRepository;

    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<ProjectSyncOutputDto, ConfigProjectSyncEntity> projectSyncMapper;
    @PostConstruct
    private void init() {
        projectSyncMapper = mapperFactory.createDtoEntityMapper(ProjectSyncOutputDto.class, ConfigProjectSyncEntity.class);
    }

    public ProjectSyncOutputDto configureJiraProjectSync(ProjectSyncInputDto projectSyncDto) {

        ConfigProjectSyncEntity existingEntity = configProjectSyncRepository.findByOrganizationIdAndProjectName(
                projectSyncDto.getOrganizationId(), projectSyncDto.getProjectName());

        ProjectSyncOutputDto configSaved;

        if (existingEntity != null) {
            configSaved = projectSyncMapper.toApi(existingEntity);
        } else {

            ConfigProjectSyncEntity configProjectSyncEntity = new ConfigProjectSyncEntity();
            configProjectSyncEntity.setId(UUID.randomUUID());
            configProjectSyncEntity.setOrganizationId(projectSyncDto.getOrganizationId());
            configProjectSyncEntity.setProjectName(projectSyncDto.getProjectName());

            configProjectSyncRepository.save(configProjectSyncEntity);

            configSaved = projectSyncMapper.toApi(configProjectSyncEntity);
        }

        return configSaved;
    }
}
