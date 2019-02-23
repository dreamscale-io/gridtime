package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.circle.*;
import com.dreamscale.htmflow.api.event.EventType;
import com.dreamscale.htmflow.api.event.NewSnippetEvent;
import com.dreamscale.htmflow.core.domain.*;
import com.dreamscale.htmflow.core.domain.flow.FlowEventEntity;
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
    CircleFeedMessageRepository circleFeedWithMembersRepository;

    @Autowired
    ActiveStatusService activeStatusService;

    @Autowired
    TimeService timeService;

    SillyNameGenerator sillyNameGenerator;

    @Autowired
    private MapperFactory mapperFactory;

    private DtoEntityMapper<CircleDto, CircleEntity> circleMapper;
    private DtoEntityMapper<CircleMemberDto, CircleMemberEntity> circleMemberMapper;
    private DtoEntityMapper<FeedMessageDto, CircleFeedEntity> feedMessageMapper;
    private DtoEntityMapper<FeedMessageDto, CircleFeedMessageEntity> circleFeedMessageMapper;

    @PostConstruct
    private void init() throws IOException, URISyntaxException {
        circleMapper = mapperFactory.createDtoEntityMapper(CircleDto.class, CircleEntity.class);
        circleMemberMapper = mapperFactory.createDtoEntityMapper(CircleMemberDto.class, CircleMemberEntity.class);
        feedMessageMapper = mapperFactory.createDtoEntityMapper(FeedMessageDto.class, CircleFeedEntity.class);
        circleFeedMessageMapper = mapperFactory.createDtoEntityMapper(FeedMessageDto.class, CircleFeedMessageEntity.class);

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
        circleFeedEntity.setTimePosition(timeService.now());

        circleFeedRepository.save(circleFeedEntity);

        CircleDto circleDto = circleMapper.toApi(circleEntity);

        List<CircleMemberDto> memberDtos = new ArrayList<>();
        memberDtos.add(createCircleMember(memberId));

        circleDto.setMembers(memberDtos);

        activeStatusService.pushWTFStatus(organizationId, memberId, circleDto.getId(), problemStatement);

        return circleDto;
    }

    public void closeCircle(UUID organizationId, UUID memberId, UUID circleId) {
        CircleEntity circleEntity = circleRepository.findOne(circleId);

        CircleFeedEntity circleFeedEntity = new CircleFeedEntity();
        circleFeedEntity.setId(UUID.randomUUID());
        circleFeedEntity.setCircleId(circleEntity.getId());
        circleFeedEntity.setMemberId(memberId);
        circleFeedEntity.setMessageType(MessageType.STATUS_UPDATE);
        circleFeedEntity.setMetadataField(CircleFeedEntity.MESSAGE_FIELD, "Circle closed.");
        circleFeedEntity.setTimePosition(timeService.now());

        circleFeedRepository.save(circleFeedEntity);

        activeStatusService.resolveWTFWithYay(organizationId, memberId);

    }

    private CircleMemberDto createCircleMember(UUID memberId, String fullName) {
        CircleMemberDto circleMemberDto = new CircleMemberDto();
        circleMemberDto.setMemberId(memberId);
        circleMemberDto.setFullName(fullName);

        return circleMemberDto;
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

    public FeedMessageDto postChatMessageToCircleFeed(UUID organizationId, UUID memberId, UUID circleId, String chatMessage) {

        CircleFeedEntity circleFeedEntity = new CircleFeedEntity();
        circleFeedEntity.setId(UUID.randomUUID());
        circleFeedEntity.setMemberId(memberId);
        circleFeedEntity.setTimePosition(timeService.now());

        circleFeedEntity.setCircleId(circleId);
        circleFeedEntity.setMetadataField(CircleFeedEntity.MESSAGE_FIELD, chatMessage);
        circleFeedEntity.setMessageType(MessageType.CHAT);

        circleFeedRepository.save(circleFeedEntity);

        FeedMessageDto feedMessageDto = feedMessageMapper.toApi(circleFeedEntity);
        feedMessageDto.setMessage(circleFeedEntity.getMetadataValue(CircleFeedEntity.MESSAGE_FIELD));

        feedMessageDto.setCircleMemberDto(createCircleMember(memberId));
        return feedMessageDto;
    }

    public List<FeedMessageDto> getAllMessagesForCircleFeed(UUID organizationId, UUID memberId, UUID circleId) {

        List<CircleFeedMessageEntity> messageEntities = circleFeedWithMembersRepository.findByCircleIdOrderByTimePosition(circleId);

        List<FeedMessageDto> feedMessageDtos = new ArrayList<>();

        for (CircleFeedMessageEntity messageEntity : messageEntities) {
            FeedMessageDto feedMessageDto = circleFeedMessageMapper.toApi(messageEntity);
            feedMessageDto.setMessage(messageEntity.getMetadataValue(CircleFeedEntity.MESSAGE_FIELD));
            feedMessageDto.setCircleMemberDto(createCircleMember(memberId, messageEntity.getFullName()));

            feedMessageDtos.add(feedMessageDto);
        }

        return feedMessageDtos;
    }

    public void saveSnippetEvent(UUID organizationId, UUID memberId, NewSnippetEvent snippetEvent) {
//        //I need the active circle,
//
//        CircleFeedEntity circleFeedEntity = new CircleFeedEntity();
//        circleFeedEntity.setId(UUID.randomUUID());
//        circleFeedEntity.setMemberId(memberId);
//        circleFeedEntity.setTimePosition(timeService.now());
//
//        circleFeedEntity.setCircleId(chatMessageInputDto.getCircleId());
//        circleFeedEntity.setMetadataField(CircleFeedEntity.MESSAGE_FIELD, chatMessageInputDto.getChatMessage());
//        circleFeedEntity.setMessageType(MessageType.CHAT);
//
//
//
//        FlowEventEntity entity = FlowEventEntity.builder()
//                .eventType(EventType.SNIPPET)
//                .memberId(memberEntity.getId())
//                .timePosition(snippetEvent.getPosition())
//                .build();
//
//        entity.setMetadataField("comment", snippetEvent.getComment());
//        entity.setMetadataField("source", snippetEvent.getSource());
//        entity.setMetadataField("snippet", snippetEvent.getSnippet());
//
//        flowEventRepository.save(entity);
    }


}
