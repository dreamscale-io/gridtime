package com.dreamscale.htmflow.core.domain.channel;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface RealtimeChannelMessageRepository extends CrudRepository<RealtimeChannelMessageEntity, UUID> {

}
