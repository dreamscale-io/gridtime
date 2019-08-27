package com.dreamscale.gridtime.core.domain.channel;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface RealtimeChannelRepository extends CrudRepository<RealtimeChannelEntity, UUID> {

}
