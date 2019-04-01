package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.core.domain.flow.*;
import com.dreamscale.htmflow.core.mapping.ProjectBuckets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class ComponentLookupService {

    @Autowired
    FlowActivityRepository flowActivityRepository;

    Map<UUID, ProjectBuckets> projectBucketsByProjectId = new HashMap<>();
    ProjectBuckets defaultProjectBuckets = new ProjectBuckets();

    public void configureMapping(UUID projectId, String bucket, String bucketMatcherWithStars) {

        ProjectBuckets projectBuckets = findOrCreateProjectBuckets(projectId);
        projectBuckets.configureBucket(bucket, bucketMatcherWithStars);
    }

    public void configureDefaultBuckets(ProjectBuckets defaultProjectBuckets) {
        this.defaultProjectBuckets = defaultProjectBuckets;
    }

    public String lookupDefaultComponent(String filePath) {
        return lookupComponent(null, filePath);
    }

    public String lookupComponent(UUID projectId, String filePath) {
        if (projectId != null) {
            ProjectBuckets projectBuckets = findOrCreateProjectBuckets(projectId);
            return projectBuckets.identifyBucket(filePath);
        } else {
            return defaultProjectBuckets.identifyBucket(filePath);
        }
    }

    private ProjectBuckets findOrCreateProjectBuckets(UUID projectId) {
        ProjectBuckets projectBuckets = projectBucketsByProjectId.get(projectId);
        if (projectBuckets == null) {
            projectBuckets = new ProjectBuckets();
            projectBucketsByProjectId.put(projectId, projectBuckets);
        }
        return projectBuckets;
    }



}
