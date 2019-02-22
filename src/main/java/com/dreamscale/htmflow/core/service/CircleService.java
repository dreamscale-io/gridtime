package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.circle.*;
import com.dreamscale.htmflow.core.domain.*;
import com.dreamscale.htmflow.core.exception.ValidationErrorCodes;
import com.dreamscale.htmflow.core.mapper.DtoEntityMapper;
import com.dreamscale.htmflow.core.mapper.MapperFactory;
import com.dreamscale.htmflow.core.mapping.SillyNameGenerator;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.InternalServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.*;
import java.util.*;

@Slf4j
@Service
public class CircleService {

    @Autowired
    CircleRepository circleRepository;

    @Autowired
    CircleMemberRepository circleMemberRepository;

    @Autowired
    CircleFeedRepository circleFeedRepository;

    @Autowired
    MemberNameRepository memberNameRepository;

    @Autowired
    TimeService timeService;

    SillyNameGenerator sillyNameGenerator;

    @Autowired
    private MapperFactory mapperFactory;

    private DtoEntityMapper<CircleDto, CircleEntity> circleMapper;
    private DtoEntityMapper<CircleMemberDto, CircleMemberEntity> circleMemberMapper;
    private DtoEntityMapper<FeedMessageDto, CircleFeedEntity> feedMessageMapper;

    @PostConstruct
    private void init() throws IOException, URISyntaxException {
        circleMapper = mapperFactory.createDtoEntityMapper(CircleDto.class, CircleEntity.class);
        circleMemberMapper = mapperFactory.createDtoEntityMapper(CircleMemberDto.class, CircleMemberEntity.class);
        feedMessageMapper = mapperFactory.createDtoEntityMapper(FeedMessageDto.class, CircleFeedEntity.class);

        sillyNameGenerator = new SillyNameGenerator();
    }


    public CircleDto createNewAdhocCircle(UUID organizationId, UUID memberId, String problemStatement) {
        CircleEntity circleEntity = new CircleEntity();
        circleEntity.setId(UUID.randomUUID());
        circleEntity.setCircleName(sillyNameGenerator.random());
        configurePublicPrivateKeyPairs(circleEntity);

        circleRepository.save(circleEntity);

        CircleMemberEntity circleMemberEntity = new CircleMemberEntity();
        circleMemberEntity.setId(UUID.randomUUID());
        circleMemberEntity.setCircleId(circleEntity.getId());
        circleMemberEntity.setMemberId(memberId);

        circleMemberRepository.save(circleMemberEntity);

        CircleFeedEntity circleFeedEntity = new CircleFeedEntity();
        circleFeedEntity.setId(UUID.randomUUID());
        circleFeedEntity.setCircleId(circleEntity.getId());
        circleFeedEntity.setMemberId(memberId);
        circleFeedEntity.setMessageType(MessageType.PROBLEM_STATEMENT);
        circleFeedEntity.setMetadataField(CircleFeedEntity.MESSAGE_FIELD, problemStatement);

        circleFeedRepository.save(circleFeedEntity);

        CircleDto circleDto = circleMapper.toApi(circleEntity);

        List<CircleMemberDto> memberDtos = new ArrayList<>();
        memberDtos.add(createCircleMember(memberId));

        circleDto.setMembers(memberDtos);

        return circleDto;
    }

    private CircleMemberDto createCircleMember(UUID memberId) {
        MemberNameEntity memberNameEntity = memberNameRepository.findOne(memberId);
        CircleMemberDto circleMemberDto = new CircleMemberDto();
        circleMemberDto.setMemberId(memberId);

        if (memberNameEntity != null) {
            circleMemberDto.setFullName(memberNameEntity.getFullName());
        }

        return circleMemberDto;
    }

    private void configurePublicPrivateKeyPairs(CircleEntity circleEntity) {

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");

            // Initialize KeyPairGenerator.
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(1024, random);

            // Generate Key Pairs, a private key and a public key.
            KeyPair keyPair = keyGen.generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();

            Base64.Encoder encoder = Base64.getEncoder();

            circleEntity.setPublicKey(encoder.encodeToString(publicKey.getEncoded()));
            circleEntity.setPrivateKey(encoder.encodeToString(privateKey.getEncoded()));
        } catch (Exception ex) {
            log.error("Unable to generate public/private keypairs", ex);
            throw new InternalServerException(ValidationErrorCodes.FAILED_PUBLICKEY_GENERATION, "Unable to generate public/private keypairs");
        }
    }

    public FeedMessageDto postChatMessageToCircleFeed(UUID organizationId, UUID memberId, ChatMessageInputDto chatMessageInputDto) {

        CircleFeedEntity circleFeedEntity = new CircleFeedEntity();
        circleFeedEntity.setId(UUID.randomUUID());
        circleFeedEntity.setMemberId(memberId);
        circleFeedEntity.setTimePosition(timeService.now());

        circleFeedEntity.setCircleId(chatMessageInputDto.getCircleId());
        circleFeedEntity.setMetadataField(CircleFeedEntity.MESSAGE_FIELD, chatMessageInputDto.getChatMessage());
        circleFeedEntity.setMessageType(MessageType.CHAT);

        circleFeedRepository.save(circleFeedEntity);

        FeedMessageDto feedMessageDto = feedMessageMapper.toApi(circleFeedEntity);
        feedMessageDto.setMessage(circleFeedEntity.getMetadataValue(CircleFeedEntity.MESSAGE_FIELD));

        feedMessageDto.setCircleMemberDto(createCircleMember(memberId));
        return feedMessageDto;
    }
}
