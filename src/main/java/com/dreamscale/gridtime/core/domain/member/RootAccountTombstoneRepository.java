package com.dreamscale.gridtime.core.domain.member;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface RootAccountTombstoneRepository extends CrudRepository<RootAccountTombstoneEntity, UUID> {


}
