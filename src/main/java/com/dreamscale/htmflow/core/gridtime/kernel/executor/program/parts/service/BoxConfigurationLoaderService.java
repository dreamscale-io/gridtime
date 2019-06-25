package com.dreamscale.htmflow.core.gridtime.kernel.executor.program.parts.service;

import com.dreamscale.htmflow.core.domain.tile.GridBoxBucketConfigEntity;
import com.dreamscale.htmflow.core.domain.tile.GridBoxBucketConfigRepository;
import com.dreamscale.htmflow.core.gridtime.kernel.executor.program.parts.sink.JSONTransformer;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.box.TeamBoxConfiguration;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.box.matcher.BoxMatcherConfig;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.box.matcher.CPGBucketConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class BoxConfigurationLoaderService {

    @Autowired
    GridBoxBucketConfigRepository gridBoxBucketConfigRepository;

    public TeamBoxConfiguration loadBoxConfiguration(UUID teamId) {

        List<GridBoxBucketConfigEntity> boxBucketConfigEntities = gridBoxBucketConfigRepository.findByTeamId(teamId);

        TeamBoxConfiguration teamBoxConfiguration = new TeamBoxConfiguration();

        for (GridBoxBucketConfigEntity boxBucketConfig : boxBucketConfigEntities) {
            UUID projectId = boxBucketConfig.getProjectId();
            BoxMatcherConfig config = JSONTransformer.fromJson(boxBucketConfig.getBoxMatcherConfigJson(), BoxMatcherConfig.class);
            teamBoxConfiguration.configureMatcher(projectId, config);
        }

        return teamBoxConfiguration;
    }

}
