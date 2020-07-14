package com.dreamscale.gridtime.core.domain.journal;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface WtfJournalLinkRepository extends CrudRepository<WtfJournalLinkEntity, UUID> {


    @Query(nativeQuery = true, value = "select wjl.* from wtf_journal_link wjl, intention i " +
            "where wjl.member_id=(:memberId) " +
            "and wjl.intention_id = i.id "+
            "and wjl.organization_id=(:organizationId) " +
            "and wjl.wtf_circuit_id=(:circuitId) " +
            "and i.finish_status is null "+
            "order by wjl.created_date desc limit 1")

    WtfJournalLinkEntity findLatestUnfinishedJournalLinkByMemberAndCircuit(@Param("organizationId") UUID organizationId,
                                                                           @Param("memberId") UUID memberId,
                                                                           @Param("circuitId") UUID circuitId);
}
