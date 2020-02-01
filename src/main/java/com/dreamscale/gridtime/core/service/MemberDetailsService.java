package com.dreamscale.gridtime.core.service;

import com.dreamscale.gridtime.api.circuit.CircuitMemberDto;
import com.dreamscale.gridtime.core.domain.member.MemberDetailsEntity;
import com.dreamscale.gridtime.core.domain.member.MemberDetailsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class MemberDetailsService {

    @Autowired
    MemberDetailsRepository memberDetailsRepository;

    public String lookupMemberName(UUID organizationId, UUID memberId) {
        String name = null;
        if (memberId != null) {
            MemberDetailsEntity memberDetails = getMemberDetails(organizationId, memberId);
            if (memberDetails != null) {
                name = memberDetails.getFullName();
            }
        }
        return name;
    }



    public MemberDetailsEntity getMemberDetails(UUID organizationId, UUID memberId) {
        return memberDetailsRepository.findByOrganizationIdAndMemberId(organizationId, memberId);
    }
}
