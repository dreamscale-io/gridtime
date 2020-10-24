package com.dreamscale.gridtime.core.machine.memory.box;

import com.dreamscale.gridtime.core.domain.tile.GridBoxBucketConfigEntity;
import com.dreamscale.gridtime.core.domain.tile.GridBoxBucketConfigRepository;
import com.dreamscale.gridtime.core.machine.commons.JSONTransformer;
import com.dreamscale.gridtime.core.machine.memory.box.matcher.BoxMatcherConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class BoxConfigLoader {

    @Autowired
    GridBoxBucketConfigRepository gridBoxBucketConfigRepository;

    private BoxConfigLoader() {}

    public ProjectBoxes loadProjectBoxesFromDB(UUID projectId) {

        List<GridBoxBucketConfigEntity> boxBucketConfigEntities = gridBoxBucketConfigRepository.findByProjectId(projectId);

        ProjectBoxes projectBoxes = null;

        if (boxBucketConfigEntities != null && boxBucketConfigEntities.size() > 0) {
            projectBoxes = new ProjectBoxes();

            for (GridBoxBucketConfigEntity boxBucketConfig : boxBucketConfigEntities) {
                BoxMatcherConfig boxMatcherConfig = JSONTransformer.fromJson(boxBucketConfig.getBoxMatcherConfigJson(), BoxMatcherConfig.class);

                projectBoxes.addBoxMatcher(boxMatcherConfig);
            }
            log.debug("Loaded "+boxBucketConfigEntities.size() + " box configurations from DB");
        } else {
            log.warn("Unable to find project boxes for projectId: "+projectId);
        }

        return projectBoxes;
    }


}
