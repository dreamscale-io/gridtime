package com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service;

import com.dreamscale.gridtime.core.domain.tile.GridBoxBucketConfigEntity;
import com.dreamscale.gridtime.core.domain.tile.GridBoxBucketConfigRepository;
import com.dreamscale.gridtime.core.machine.commons.JSONTransformer;
import com.dreamscale.gridtime.core.machine.memory.box.TeamBoxConfiguration;
import com.dreamscale.gridtime.core.machine.memory.box.matcher.BoxMatcherConfig;
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
