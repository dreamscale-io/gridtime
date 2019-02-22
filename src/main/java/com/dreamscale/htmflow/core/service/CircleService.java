package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.circle.CircleDto;
import com.dreamscale.htmflow.api.circle.CircleMemberDto;
import com.dreamscale.htmflow.api.circle.SnippetType;
import com.dreamscale.htmflow.api.organization.TeamMemberWorkStatusDto;
import com.dreamscale.htmflow.api.status.WtfStatusInputDto;
import com.dreamscale.htmflow.core.domain.*;
import com.dreamscale.htmflow.core.mapper.DtoEntityMapper;
import com.dreamscale.htmflow.core.mapper.MapperFactory;
import com.dreamscale.htmflow.core.mapping.SillyNameGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class CircleService {

    @Autowired
    CircleRepository circleRepository;

    @Autowired
    CircleMemberRepository circleMemberRepository;

    @Autowired
    CircleSnippetRepository circleSnippetRepository;

    @Autowired
    TimeService timeService;

    SillyNameGenerator sillyNameGenerator;

    @Autowired
    private MapperFactory mapperFactory;

    private DtoEntityMapper<CircleDto, CircleEntity> circleMapper;
    private DtoEntityMapper<CircleMemberDto, CircleMemberEntity> circleMemberMapper;

    @PostConstruct
    private void init() throws IOException, URISyntaxException {
        circleMapper = mapperFactory.createDtoEntityMapper(CircleDto.class, CircleEntity.class);
        circleMemberMapper = mapperFactory.createDtoEntityMapper(CircleMemberDto.class, CircleMemberEntity.class);
        sillyNameGenerator = new SillyNameGenerator();
    }


    public CircleDto createNewAdhocCircle(UUID organizationId, UUID memberId, String problemStatement) {
        CircleEntity circleEntity = new CircleEntity();
        circleEntity.setId(UUID.randomUUID());
        circleEntity.setCircleName(sillyNameGenerator.random());

        circleRepository.save(circleEntity);

        CircleMemberEntity circleMemberEntity = new CircleMemberEntity();
        circleMemberEntity.setId(UUID.randomUUID());
        circleMemberEntity.setCircleId(circleEntity.getId());
        circleMemberEntity.setMemberId(memberId);

        circleMemberRepository.save(circleMemberEntity);

        CircleSnippetEntity circleSnippetEntity = new CircleSnippetEntity();
        circleSnippetEntity.setId(UUID.randomUUID());
        circleSnippetEntity.setCircleId(circleEntity.getId());
        circleSnippetEntity.setSnippetType(SnippetType.PROBLEM_STATEMENT);
        circleSnippetEntity.setMetadataField("problem_statement", problemStatement);

        circleSnippetRepository.save(circleSnippetEntity);

        CircleDto circleDto = circleMapper.toApi(circleEntity);
        CircleMemberDto circleMember = circleMemberMapper.toApi(circleMemberEntity);

        List<CircleMemberDto> memberDtos = new ArrayList<>();
        memberDtos.add(circleMember);

        circleDto.setMembers(memberDtos);

        return circleDto;
    }
}
