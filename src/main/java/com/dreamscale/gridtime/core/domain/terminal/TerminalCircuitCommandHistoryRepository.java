package com.dreamscale.gridtime.core.domain.terminal;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface TerminalCircuitCommandHistoryRepository extends CrudRepository<TerminalCircuitCommandHistoryEntity, UUID> {

}
