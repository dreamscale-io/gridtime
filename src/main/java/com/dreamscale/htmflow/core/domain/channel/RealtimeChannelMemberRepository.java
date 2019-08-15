package com.dreamscale.htmflow.core.domain.channel;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface RealtimeChannelMemberRepository extends CrudRepository<RealtimeChannelMemberEntity, UUID> {

    RealtimeChannelMemberEntity findByChannelIdAndMemberId(UUID channelId, UUID memberId);

    List<RealtimeChannelMemberEntity> findByChannelId(UUID channelId);
}
