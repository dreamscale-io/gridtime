package com.dreamscale.gridtime.core.domain.member;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OneTimeTicketRepository extends CrudRepository<OneTimeTicketEntity, UUID> {

    OneTimeTicketEntity findByTicketCode(String ticketCode);
}
