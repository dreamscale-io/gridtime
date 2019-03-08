package com.dreamscale.htmflow.core.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface SpiritXPRepository extends CrudRepository<SpiritXPEntity, UUID> {

    SpiritXPEntity findBySpiritId(UUID spiritId);

}
