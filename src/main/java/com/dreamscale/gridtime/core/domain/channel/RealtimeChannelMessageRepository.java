package com.dreamscale.gridtime.core.domain.channel;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface RealtimeChannelMessageRepository extends CrudRepository<RealtimeChannelMessageEntity, UUID> {

    List<RealtimeChannelMessageEntity> findByChannelIdOrderByMessageTime(UUID channelId);
}
