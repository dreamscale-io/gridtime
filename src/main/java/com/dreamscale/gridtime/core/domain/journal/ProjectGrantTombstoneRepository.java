package com.dreamscale.gridtime.core.domain.journal;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface ProjectGrantTombstoneRepository extends CrudRepository<ProjectGrantTombstoneEntity, UUID> {

}
