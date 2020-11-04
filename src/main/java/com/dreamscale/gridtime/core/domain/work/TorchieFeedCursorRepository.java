package com.dreamscale.gridtime.core.domain.work;

import com.dreamscale.gridtime.core.domain.member.TeamMemberEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TorchieFeedCursorRepository extends CrudRepository<TorchieFeedCursorEntity, UUID> {


    TorchieFeedCursorEntity findByTorchieId(UUID torchieId);

    @Query(nativeQuery = true, value = "select * from torchie_feed_cursor " +
            "where claiming_server_id is null "+
            " and (last_published_data_cursor > next_wait_until_cursor) " +
            "order by last_claim_update limit (:limit)")
    List<TorchieFeedCursorEntity> findUnclaimedTorchies(@Param("limit") int limit);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "update torchie_feed_cursor " +
            "set last_tile_processed_cursor = (:lastProcessedDate), "+
            " next_wait_until_cursor = (:nextWaitUntilDate) "+
            "where torchie_id = (:torchieId) ")
    void updateLastProcessed(@Param("torchieId") UUID torchieId,
                             @Param("lastProcessedDate") Timestamp lastProcessed,
                             @Param("nextWaitUntilDate") Timestamp nextWaitUntil
                             );

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "update torchie_feed_cursor " +
            "set last_published_data_cursor = (:lastPublishedDate) "+
            "where torchie_id = (:torchieId) ")
    void updateLastPublished(@Param("torchieId") UUID torchieId, @Param("lastPublishedDate") Timestamp lastPublishedDate);




    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "update torchie_feed_cursor " +
            "set claiming_server_id = null "+
            "where torchie_id = (:torchieId) ")
    void expire(@Param("torchieId") UUID torchieId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "update torchie_feed_cursor " +
            "set claiming_server_id = null "+
            "where claiming_server_id is not null and last_claim_update < (:expireBeforeDate) ")
    void expireZombieTorchies(@Param("expireBeforeDate") Timestamp expireBeforeDate);

    @Modifying
    @Query(nativeQuery = true, value = "truncate table torchie_feed_cursor")
    void truncate();
}

