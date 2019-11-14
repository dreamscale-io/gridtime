package com.dreamscale.gridtime.core.machine.executor.worker;

import com.dreamscale.gridtime.core.domain.work.TorchieFeedCursorEntity;
import com.dreamscale.gridtime.core.domain.work.TorchieFeedCursorRepository;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class BookmarkService {

    @Autowired
    private TorchieFeedCursorRepository torchieFeedCursorRepository;

    public void updateLastPublishedData(UUID torchieId, LocalDateTime lastPublishedDate) {
        torchieFeedCursorRepository.updateLastPublished(torchieId, Timestamp.valueOf(lastPublishedDate));
    }


    public void updateLastProcessed(UUID torchieId, LocalDateTime lastProcessed) {
        torchieFeedCursorRepository.updateLastProcessed(torchieId, Timestamp.valueOf(lastProcessed),
                Timestamp.valueOf(lastProcessed.plus(ZoomLevel.TWENTY.getDuration())));
    }

    public void updateFailedProcess(UUID torchieId) {
        TorchieFeedCursorEntity cursor = torchieFeedCursorRepository.findOne(torchieId);

        if (cursor.getFailureCount() == null || cursor.getFailureCount() == 0) {
            cursor.setFailureCount(1);
        } else {
            cursor.setFailureCount(cursor.getFailureCount() +1);
        }

        torchieFeedCursorRepository.save(cursor);
    }

}
