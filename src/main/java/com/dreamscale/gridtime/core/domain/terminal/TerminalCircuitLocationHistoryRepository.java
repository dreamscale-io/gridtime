package com.dreamscale.gridtime.core.domain.terminal;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface TerminalCircuitLocationHistoryRepository extends CrudRepository<TerminalCircuitLocationHistoryEntity, UUID> {

}
