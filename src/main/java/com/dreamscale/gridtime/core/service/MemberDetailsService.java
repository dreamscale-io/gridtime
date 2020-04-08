package com.dreamscale.gridtime.core.service;

import com.dreamscale.gridtime.core.domain.member.MemberDetailsEntity;
import com.dreamscale.gridtime.core.domain.member.MemberDetailsRepository;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class MemberDetailsService {

    @Autowired
    MemberDetailsRepository memberDetailsRepository;

    @Autowired
    OrganizationMemberRepository organizationMemberRepository;

    public String lookupMemberName(UUID organizationId, UUID memberId) {
        String name = null;
        if (memberId != null) {
            MemberDetailsEntity memberDetails = lookupMemberDetails(organizationId, memberId);
            if (memberDetails != null) {
                name = memberDetails.getFullName();
            }
        }
        return name;
    }

    public String lookupUserName(UUID memberId) {
        String username = null;
        if (memberId != null) {
            OrganizationMemberEntity memberEntity = organizationMemberRepository.findById(memberId);
            if (memberEntity != null) {
                username = memberEntity.getUsername();
            }
        }

        return username;
    }

    public MemberDetailsEntity lookupMemberDetails(UUID memberId) {
        return memberDetailsRepository.findByMemberId(memberId);
    }

    public MemberDetailsEntity lookupMemberDetails(UUID organizationId, UUID memberId) {
        return memberDetailsRepository.findByOrganizationIdAndMemberId(organizationId, memberId);
    }


}
