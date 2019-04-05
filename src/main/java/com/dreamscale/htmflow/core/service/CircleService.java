package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.circle.*;
import com.dreamscale.htmflow.api.event.NewSnippetEvent;
import com.dreamscale.htmflow.api.journal.FinishStatus;
import com.dreamscale.htmflow.api.journal.JournalEntryDto;
import com.dreamscale.htmflow.core.domain.*;
import com.dreamscale.htmflow.core.exception.ValidationErrorCodes;
import com.dreamscale.htmflow.core.mapper.DtoEntityMapper;
import com.dreamscale.htmflow.core.mapper.MapperFactory;
import com.dreamscale.htmflow.core.mapping.SillyNameGenerator;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.dreamscale.exception.InternalServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    CircleMessageRepository circleFeedWithMembersRepository;

    @Autowired
    ActiveStatusService activeStatusService;

    @Autowired
    CircleContextRepository circleContextRepository;

    @Autowired
    IntentionRepository intentionRepository;

    @Autowired
    JournalEntryRepository journalEntryRepository;

    @Autowired
    TimeService timeService;

    SillyNameGenerator sillyNameGenerator;

    @Autowired
    private MapperFactory mapperFactory;

    private DtoEntityMapper<CircleDto, CircleEntity> circleMapper;
    private DtoEntityMapper<CircleMemberDto, CircleMemberEntity> circleMemberMapper;
    private DtoEntityMapper<FeedMessageDto, CircleMessageEntity> feedMessageMapper;
    private DtoEntityMapper<FeedMessageDto, CircleFeedMessageEntity> circleFeedMessageMapper;
    private DtoEntityMapper<JournalEntryDto, JournalEntryEntity> journalEntryMapper;

    @PostConstruct
    private void init() throws IOException, URISyntaxException {
        circleMapper = mapperFactory.createDtoEntityMapper(CircleDto.class, CircleEntity.class);
        circleMemberMapper = mapperFactory.createDtoEntityMapper(CircleMemberDto.class, CircleMemberEntity.class);
        feedMessageMapper = mapperFactory.createDtoEntityMapper(FeedMessageDto.class, CircleMessageEntity.class);
        circleFeedMessageMapper = mapperFactory.createDtoEntityMapper(FeedMessageDto.class, CircleFeedMessageEntity.class);
        journalEntryMapper = mapperFactory.createDtoEntityMapper(JournalEntryDto.class, JournalEntryEntity.class);
        sillyNameGenerator = new SillyNameGenerator();
    }

    public List<CircleDto> getAllOpenCircles(UUID organizationId, UUID spiritId) {
        List<CircleEntity> circleEntities = circleRepository.findAllOpenCirclesForOrganization(organizationId);
        List<CircleDto> circles = circleMapper.toApiList(circleEntities);

        for (CircleDto circle: circles) {
            circle.setDurationInSeconds(calculateEffectiveDuration(circle));
        }
        return circles;
    }

    public List<CircleDto> getAllDoItLaterCircles(UUID organizationId, UUID spiritId) {
        List<CircleEntity> circleEntities = circleRepository.findAllDoItLaterCircles(organizationId, spiritId);
        return circleMapper.toApiList(circleEntities);

    }

    public CircleDto createNewAdhocCircle(UUID organizationId, UUID spiritId, String problemStatement) {
        CircleEntity circleEntity = new CircleEntity();
        circleEntity.setId(UUID.randomUUID());
        circleEntity.setCircleName(sillyNameGenerator.random());
        circleEntity.setStartTime(timeService.now());
        circleEntity.setOwnerMemberId(spiritId);
        circleEntity.setDurationInSeconds(0L);
        circleEntity.setProblemDescription(problemStatement);
        circleEntity.setOrganizationId(organizationId);
        circleEntity.setOnShelf(false);

        configurePublicPrivateKeyPairs(circleEntity);

        circleRepository.save(circleEntity);

        CircleMemberEntity circleMemberEntity = new CircleMemberEntity();
        circleMemberEntity.setId(UUID.randomUUID());
        circleMemberEntity.setCircleId(circleEntity.getId());
        circleMemberEntity.setSpiritId(spiritId);

        circleMemberRepository.save(circleMemberEntity);

        CircleContextEntity circleContextEntity = createCircleContextEntity(organizationId, spiritId, circleEntity);
        circleContextRepository.save(circleContextEntity);

        CircleMessageEntity circleMessageEntity = new CircleMessageEntity();
        circleMessageEntity.setId(UUID.randomUUID());
        circleMessageEntity.setCircleId(circleEntity.getId());
        circleMessageEntity.setSpiritId(spiritId);
        circleMessageEntity.setMessageType(CircleMessageType.CIRCLE_START);
        circleMessageEntity.setMetadataField(CircleMessageEntity.MESSAGE_FIELD, problemStatement);
        circleMessageEntity.setPosition(timeService.now());

        circleFeedRepository.save(circleMessageEntity);

        CircleDto circleDto = circleMapper.toApi(circleEntity);

        List<CircleMemberDto> memberDtos = new ArrayList<>();
        memberDtos.add(createCircleMember(spiritId));
        circleDto.setMembers(memberDtos);

        activeStatusService.pushWTFStatus(organizationId, spiritId, circleDto.getId(), problemStatement);

        return circleDto;
    }

    public CircleDto getActiveCircle(UUID organizationId, UUID memberId) {

        UUID activeCircleId = activeStatusService.getActiveCircleId(organizationId, memberId);

        CircleDto activeCircle = null;
        if (activeCircleId != null) {
            activeCircle = getCircle(memberId, activeCircleId);
        }

        return activeCircle;
    }

    public CircleDto closeCircle(UUID organizationId, UUID spiritId, UUID circleId) {
        CircleEntity circleEntity = circleRepository.findOne(circleId);
        circleEntity.setEndTime(timeService.now());

        long durationInSeconds = calculateDuration(circleEntity, timeService.now());
        circleEntity.setDurationInSeconds(durationInSeconds);

        circleEntity.setOnShelf(false);
        circleEntity.setLastShelfTime(null);
        circleEntity.setLastResumeTime(null);

        circleRepository.save(circleEntity);

        CircleContextEntity circleContextEntity = circleContextRepository.findLastByCircleId(circleId);
        if (circleContextEntity != null) {
            circleContextEntity.setFinishStatus(FinishStatus.done.name());
            circleContextEntity.setFinishTime(circleEntity.getEndTime());

            circleContextRepository.save(circleContextEntity);
        }

        CircleMessageEntity circleMessageEntity = new CircleMessageEntity();
        circleMessageEntity.setId(UUID.randomUUID());
        circleMessageEntity.setCircleId(circleEntity.getId());
        circleMessageEntity.setSpiritId(spiritId);
        circleMessageEntity.setMessageType(CircleMessageType.CIRCLE_CLOSED);
        circleMessageEntity.setMetadataField(CircleMessageEntity.MESSAGE_FIELD, "Circle closed.");
        circleMessageEntity.setPosition(timeService.now());

        circleFeedRepository.save(circleMessageEntity);

        activeStatusService.resolveWTFWithYay(organizationId, spiritId);

        CircleDto circleDto = circleMapper.toApi(circleEntity);

        return circleDto;
    }

    public CircleDto shelveCircleWithDoItLater(UUID organizationId, UUID spiritId, UUID circleId) {
        CircleEntity circleEntity = circleRepository.findOne(circleId);

        long durationInSeconds = calculateDuration(circleEntity, timeService.now());
        circleEntity.setDurationInSeconds(durationInSeconds);

        circleEntity.setOnShelf(true);
        circleEntity.setLastShelfTime(timeService.now());
        circleEntity.setLastResumeTime(null);

        circleRepository.save(circleEntity);

        CircleContextEntity circleContextEntity = circleContextRepository.findLastByCircleId(circleId);
        if (circleContextEntity != null) {
            circleContextEntity.setFinishStatus(FinishStatus.aborted.name());
            circleContextEntity.setFinishTime(timeService.now());

            circleContextRepository.save(circleContextEntity);
        }

        CircleMessageEntity circleMessageEntity = new CircleMessageEntity();
        circleMessageEntity.setId(UUID.randomUUID());
        circleMessageEntity.setCircleId(circleEntity.getId());
        circleMessageEntity.setSpiritId(spiritId);
        circleMessageEntity.setMessageType(CircleMessageType.CIRCLE_SHELVED);
        circleMessageEntity.setMetadataField(CircleMessageEntity.MESSAGE_FIELD, "Circle placed on 'Do It Later' shelf.");
        circleMessageEntity.setPosition(timeService.now());

        circleFeedRepository.save(circleMessageEntity);

        activeStatusService.resolveWTFWithAbort(organizationId, spiritId);

        CircleDto circleDto = circleMapper.toApi(circleEntity);

        return circleDto;

    }

    public CircleDto resumeAnExistingCircleFromDoItLaterShelf(UUID organizationId, UUID spiritId, UUID circleId) {
        CircleEntity circleEntity = circleRepository.findOne(circleId);

        circleEntity.setOnShelf(false);
        circleEntity.setLastResumeTime(timeService.now());
        circleEntity.setLastShelfTime(null);

        circleRepository.save(circleEntity);

        CircleContextEntity circleContextEntity = createCircleContextFromHistoricalContext(organizationId, spiritId, circleEntity);
        circleContextRepository.save(circleContextEntity);

        CircleMessageEntity circleMessageEntity = new CircleMessageEntity();
        circleMessageEntity.setId(UUID.randomUUID());
        circleMessageEntity.setCircleId(circleEntity.getId());
        circleMessageEntity.setSpiritId(spiritId);
        circleMessageEntity.setMessageType(CircleMessageType.CIRCLE_RESUMED);
        circleMessageEntity.setMetadataField(CircleMessageEntity.MESSAGE_FIELD, "Circle resumed from 'Do It Later' shelf.");
        circleMessageEntity.setPosition(timeService.now());

        circleFeedRepository.save(circleMessageEntity);

        activeStatusService.pushWTFStatus(organizationId, spiritId, circleId, circleEntity.getProblemDescription());

        CircleDto circleDto = circleMapper.toApi(circleEntity);
        circleDto.setDurationInSeconds(calculateEffectiveDuration(circleDto));

        return circleDto;
    }


    private CircleContextEntity createCircleContextFromHistoricalContext(UUID organizationId, UUID spiritId, CircleEntity circleEntity) {
        CircleContextEntity circleContextEntity = new CircleContextEntity();
        circleContextEntity.setId(UUID.randomUUID());
        circleContextEntity.setSpiritId(spiritId);
        circleContextEntity.setOrganizationId(organizationId);
        circleContextEntity.setDescription(circleEntity.getProblemDescription());
        circleContextEntity.setCircleId(circleEntity.getId());

        CircleContextEntity historicalContext = circleContextRepository.findFirstByCircleId(circleEntity.getId());

        if (historicalContext != null) {
            circleContextEntity.setProjectId(historicalContext.getProjectId());
            circleContextEntity.setTaskId(historicalContext.getTaskId());
        }
        return circleContextEntity;
    }

    private CircleContextEntity createCircleContextEntity(UUID organizationId, UUID spiritId, CircleEntity circleEntity) {

        CircleContextEntity circleContextEntity = new CircleContextEntity();
        circleContextEntity.setId(UUID.randomUUID());
        circleContextEntity.setSpiritId(spiritId);
        circleContextEntity.setOrganizationId(organizationId);
        circleContextEntity.setPosition(timeService.now());
        circleContextEntity.setDescription(circleEntity.getProblemDescription());
        circleContextEntity.setCircleId(circleEntity.getId());

        List<IntentionEntity> lastIntentionList = intentionRepository.findByMemberIdWithLimit(spiritId, 1);

        if (lastIntentionList.size() > 0) {
            IntentionEntity lastIntention = lastIntentionList.get(0);
            circleContextEntity.setProjectId(lastIntention.getProjectId());
            circleContextEntity.setTaskId(lastIntention.getTaskId());
        }
        return circleContextEntity;
    }

    private long calculateDuration(CircleEntity circleEntity, LocalDateTime now) {
        long totalDuration = 0;

        if (circleEntity.getDurationInSeconds() != null) {
            totalDuration = circleEntity.getDurationInSeconds();
        }

        //either take the additional time from start, or from resume
        if (circleEntity.getLastResumeTime() == null) {
            long additionalDuration = ChronoUnit.SECONDS.between(circleEntity.getStartTime(), now);
            totalDuration += additionalDuration;
        } else {
            long additionalDuration = ChronoUnit.SECONDS.between(circleEntity.getLastResumeTime(), now);
            totalDuration += additionalDuration;
        }

        return totalDuration;
    }

    private CircleMemberDto createCircleMember(UUID spiritId, String fullName) {
        CircleMemberDto circleMemberDto = new CircleMemberDto();
        circleMemberDto.setSpiritId(spiritId);
        circleMemberDto.setFullName(fullName);

        return circleMemberDto;
    }

    private CircleMemberDto createCircleMember(UUID memberId) {
        MemberNameEntity memberNameEntity = memberNameRepository.findOne(memberId);
        CircleMemberDto circleMemberDto = new CircleMemberDto();
        circleMemberDto.setSpiritId(memberId);

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

    public FeedMessageDto postChatMessageToCircleFeed(UUID organizationId, UUID spiritId, UUID circleId, String chatMessage) {

        CircleMessageEntity circleMessageEntity = new CircleMessageEntity();
        circleMessageEntity.setId(UUID.randomUUID());
        circleMessageEntity.setSpiritId(spiritId);
        circleMessageEntity.setPosition(timeService.now());

        circleMessageEntity.setCircleId(circleId);
        circleMessageEntity.setMetadataField(CircleMessageEntity.MESSAGE_FIELD, chatMessage);
        circleMessageEntity.setMessageType(CircleMessageType.CHAT);

        circleFeedRepository.save(circleMessageEntity);

        FeedMessageDto feedMessageDto = feedMessageMapper.toApi(circleMessageEntity);
        feedMessageDto.setMessage(circleMessageEntity.getMetadataValue(CircleMessageEntity.MESSAGE_FIELD));

        feedMessageDto.setCircleMemberDto(createCircleMember(spiritId));
        return feedMessageDto;
    }

    public FeedMessageDto postScreenshotReferenceToCircleFeed(UUID organizationId, UUID spiritId, UUID circleId, ScreenshotReferenceInputDto screenshotReferenceInputDto) {
        CircleMessageEntity circleMessageEntity = new CircleMessageEntity();
        circleMessageEntity.setId(UUID.randomUUID());
        circleMessageEntity.setSpiritId(spiritId);
        circleMessageEntity.setPosition(timeService.now());

        circleMessageEntity.setCircleId(circleId);
        circleMessageEntity.setMetadataField(CircleMessageEntity.MESSAGE_FIELD, "Added screenshot for "+screenshotReferenceInputDto.getFileName());
        circleMessageEntity.setMetadataField(CircleMessageEntity.FILE_NAME_FIELD, screenshotReferenceInputDto.getFileName());
        circleMessageEntity.setMetadataField(CircleMessageEntity.FILEPATH_FIELD, screenshotReferenceInputDto.getFilePath());
        circleMessageEntity.setMessageType(CircleMessageType.SCREENSHOT);

        circleFeedRepository.save(circleMessageEntity);

        FeedMessageDto feedMessageDto = feedMessageMapper.toApi(circleMessageEntity);

        feedMessageDto.setMessage(circleMessageEntity.getMetadataValue(CircleMessageEntity.MESSAGE_FIELD));
        feedMessageDto.setFileName(circleMessageEntity.getMetadataValue(CircleMessageEntity.FILE_NAME_FIELD));
        feedMessageDto.setFilePath(circleMessageEntity.getMetadataValue(CircleMessageEntity.FILEPATH_FIELD));

        feedMessageDto.setCircleMemberDto(createCircleMember(spiritId));
        return feedMessageDto;

    }

    CircleDto getCircle(UUID spiritId, UUID circleId) {
        CircleDto circleDto = null;

        CircleEntity circleEntity = circleRepository.findOne(circleId);
        circleDto = circleMapper.toApi(circleEntity);
        circleDto.setDurationInSeconds(calculateEffectiveDuration(circleDto));

        List<CircleMemberDto> memberDtos = new ArrayList<>();
        memberDtos.add(createCircleMember(spiritId));

        circleDto.setMembers(memberDtos);


        return circleDto;
    }

    private CircleDto toCircleDto(CircleEntity circleEntity, List<MemberNameEntity> circleMemberEntities) {
        CircleDto circleDto = circleMapper.toApi(circleEntity);
        circleDto.setDurationInSeconds(calculateEffectiveDuration(circleDto));

        List<CircleMemberDto> memberDtos = new ArrayList<>();

        for (MemberNameEntity circleMember : circleMemberEntities) {
            CircleMemberDto circleMemberDto = new CircleMemberDto();
            circleMemberDto.setSpiritId(circleMember.getSpiritId());
            circleMemberDto.setFullName(circleMember.getFullName());

            memberDtos.add(circleMemberDto);
        }

        circleDto.setMembers(memberDtos);

        return circleDto;
    }


    private Long calculateEffectiveDuration(CircleDto circleDto) {
        LocalDateTime startTimer = circleDto.getStartTime();

        if (circleDto.getLastResumeTime() != null) {
            startTimer = circleDto.getLastResumeTime();
        }

        long seconds = startTimer.until( timeService.now(), ChronoUnit.SECONDS);
        seconds += circleDto.getDurationInSeconds();

        return seconds;
    }

    public FeedMessageDto postSnippetToActiveCircleFeed(UUID organizationId, UUID spiritId, NewSnippetEvent snippetEvent) {

        UUID activeCircleId = activeStatusService.getActiveCircleId(organizationId, spiritId);

        FeedMessageDto feedMessageDto = null;

        if (activeCircleId != null) {

            CircleMessageEntity circleMessageEntity = new CircleMessageEntity();
            circleMessageEntity.setId(UUID.randomUUID());
            circleMessageEntity.setSpiritId(spiritId);
            circleMessageEntity.setPosition(timeService.now());

            circleMessageEntity.setCircleId(activeCircleId);
            circleMessageEntity.setMetadataField(CircleMessageEntity.MESSAGE_FIELD, "Added snippet from "+snippetEvent.getSource());
            circleMessageEntity.setMetadataField(CircleMessageEntity.SNIPPET_SOURCE_FIELD, snippetEvent.getSource());
            circleMessageEntity.setMetadataField(CircleMessageEntity.SNIPPET_FIELD, snippetEvent.getSnippet());
            circleMessageEntity.setMessageType(CircleMessageType.SNIPPET);

            circleFeedRepository.save(circleMessageEntity);

            feedMessageDto = feedMessageMapper.toApi(circleMessageEntity);

            feedMessageDto.setMessage(circleMessageEntity.getMetadataValue(CircleMessageEntity.MESSAGE_FIELD));
            feedMessageDto.setSnippetSource(circleMessageEntity.getMetadataValue(CircleMessageEntity.SNIPPET_SOURCE_FIELD));
            feedMessageDto.setSnippet(circleMessageEntity.getMetadataValue(CircleMessageEntity.SNIPPET_FIELD));

            feedMessageDto.setCircleMemberDto(createCircleMember(spiritId));
        }

        return feedMessageDto;

    }


    public List<FeedMessageDto> getAllMessagesForCircleFeed(UUID organizationId, UUID spiritId, UUID circleId) {

        List<CircleFeedMessageEntity> messageEntities = circleFeedWithMembersRepository.findByCircleIdOrderByPosition(circleId);

        List<FeedMessageDto> feedMessageDtos = new ArrayList<>();

        for (CircleFeedMessageEntity messageEntity : messageEntities) {
            FeedMessageDto feedMessageDto = circleFeedMessageMapper.toApi(messageEntity);
            feedMessageDto.setMessage(messageEntity.getMetadataValue(CircleMessageEntity.MESSAGE_FIELD));
            feedMessageDto.setCircleMemberDto(createCircleMember(spiritId, messageEntity.getFullName()));

            feedMessageDtos.add(feedMessageDto);
        }

        return feedMessageDtos;
    }


    public CircleKeyDto retrieveKey(UUID organizationId, UUID spiritId, UUID circleId) {
        CircleKeyDto circleKeyDto = null;
        CircleEntity circleEntity = circleRepository.findByOwnerMemberIdAndId(spiritId, circleId);
        if (circleEntity != null) {
            circleKeyDto = new CircleKeyDto();
            circleKeyDto.setPrivateKey(circleEntity.getPrivateKey());
        } else {
            throw new BadRequestException(ValidationErrorCodes.NO_ACCESS_TO_CIRCLE_KEY, "Unable to retrieve circle key");
        }

        return circleKeyDto;
    }


    public List<CircleDto> getAllParticipatingCircles(UUID organizationId, UUID spiritId) {
        List<CircleDto> circles = new ArrayList<>();

        List<CircleEntity> circleEntities = circleRepository.findAllByParticipation(organizationId, spiritId);

        for (CircleEntity circleEntity : circleEntities) {
            List<MemberNameEntity> circleMembers = memberNameRepository.findAllByCircleId(circleEntity.getId());

            CircleDto circleDto = this.toCircleDto(circleEntity, circleMembers);
            circles.add(circleDto);

        }

        return circles;
    }


}
