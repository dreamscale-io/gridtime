package com.dreamscale.gridtime.core.domain.dictionary;

import com.dreamscale.gridtime.core.domain.circuit.TalkRoomEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface CommunityBookRepository extends CrudRepository<CommunityBookEntity, UUID> {

}
