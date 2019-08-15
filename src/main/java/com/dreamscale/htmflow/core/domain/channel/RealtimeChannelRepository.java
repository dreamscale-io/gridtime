package com.dreamscale.htmflow.core.domain.channel;

import com.dreamscale.htmflow.core.domain.time.GridTimeCalendarEntity;
import com.dreamscale.htmflow.core.gridtime.machine.clock.ZoomLevel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface RealtimeChannelRepository extends CrudRepository<RealtimeChannelEntity, UUID> {

}
