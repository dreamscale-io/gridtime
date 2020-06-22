package com.dreamscale.gridtime.core.capability.active;

import com.dreamscale.gridtime.api.organization.MemberDetailsDto;
import com.dreamscale.gridtime.api.organization.OrganizationDto;
import com.dreamscale.gridtime.api.organization.OrganizationSubscriptionDto;
import com.dreamscale.gridtime.api.organization.SubscriptionInputDto;
import com.dreamscale.gridtime.core.domain.member.*;
import com.dreamscale.gridtime.core.mapper.DtoEntityMapper;
import com.dreamscale.gridtime.core.mapper.MapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class MemberDetailsRetriever {

    @Autowired
    MemberDetailsRepository memberDetailsRepository;

    @Autowired
    OrganizationMemberRepository organizationMemberRepository;

    private DtoEntityMapper<MemberDetailsDto, MemberDetailsEntity> memberDetailsMapper;

    @Autowired
    private MapperFactory mapperFactory;

    @PostConstruct
    private void init() {
        memberDetailsMapper = mapperFactory.createDtoEntityMapper(MemberDetailsDto.class, MemberDetailsEntity.class);
    }

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

    public String lookupUsername(UUID memberId) {
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


    public List<MemberDetailsDto> getOrganizationMembers(UUID organizationId) {
        List<MemberDetailsEntity> memberDetailsList = memberDetailsRepository.findByOrganizationId(organizationId);

        return memberDetailsMapper.toApiList(memberDetailsList);
    }
}
