package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.circle.*;
import com.dreamscale.htmflow.api.event.NewSnippetEvent;
import com.dreamscale.htmflow.api.journal.FinishStatus;
import com.dreamscale.htmflow.api.journal.JournalEntryDto;
import com.dreamscale.htmflow.core.domain.circle.*;
import com.dreamscale.htmflow.core.domain.journal.IntentionEntity;
import com.dreamscale.htmflow.core.domain.journal.IntentionRepository;
import com.dreamscale.htmflow.core.domain.journal.JournalEntryEntity;
import com.dreamscale.htmflow.core.domain.journal.JournalEntryRepository;
import com.dreamscale.htmflow.core.domain.member.MemberNameEntity;
import com.dreamscale.htmflow.core.domain.member.MemberNameRepository;
import com.dreamscale.htmflow.core.exception.ValidationErrorCodes;
import com.dreamscale.htmflow.core.mapper.DtoEntityMapper;
import com.dreamscale.htmflow.core.mapper.MapperFactory;
import com.dreamscale.htmflow.core.mapping.SillyNameGenerator;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
    CircleMessageRepository circleMessageRepository;

    @Autowired
    MemberNameRepository memberNameRepository;

    @Autowired
    CircleFeedMessageRepository circleFeedWithMembersRepository;

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


    @Autowired
    TeamService teamService;

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

    public List<CircleDto> getAllOpenCircles(UUID organizationId, UUID torchieId) {
        List<CircleEntity> circleEntities = circleRepository.findAllOpenCirclesForOrganization(organizationId);
        List<CircleDto> circles = circleMapper.toApiList(circleEntities);

        for (CircleDto circle : circles) {
            circle.setDurationInSeconds(calculateEffectiveDuration(circle));
        }
        return circles;
    }

    public List<CircleDto> getAllDoItLaterCircles(UUID organizationId, UUID torchieId) {
        List<CircleEntity> circleEntities = circleRepository.findAllDoItLaterCircles(organizationId, torchieId);
        return circleMapper.toApiList(circleEntities);

    }

    public CircleDto createNewAdhocCircle(UUID organizationId, UUID torchieId, String problemStatement) {
        CircleEntity circleEntity = new CircleEntity();
        circleEntity.setId(UUID.randomUUID());
        circleEntity.setCircleName(sillyNameGenerator.random());
        circleEntity.setStartTime(timeService.now());
        circleEntity.setOwnerMemberId(torchieId);
        circleEntity.setDurationInSeconds(0L);
        circleEntity.setProblemDescription(problemStatement);
        circleEntity.setOrganizationId(organizationId);
        circleEntity.setOnShelf(false);

        circleEntity.setChannelId(UUID.randomUUID());

        circleRepository.save(circleEntity);

        CircleMemberEntity circleMemberEntity = new CircleMemberEntity();
        circleMemberEntity.setId(UUID.randomUUID());
        circleMemberEntity.setCircleId(circleEntity.getId());
        circleMemberEntity.setTorchieId(torchieId);

        circleMemberRepository.save(circleMemberEntity);

        CircleContextEntity circleContextEntity = createCircleContextEntity(organizationId, torchieId, circleEntity);
        circleContextRepository.save(circleContextEntity);

        CircleMessageEntity circleMessageEntity = new CircleMessageEntity();
        circleMessageEntity.setId(UUID.randomUUID());
        circleMessageEntity.setCircleId(circleEntity.getId());
        circleMessageEntity.setTorchieId(torchieId);
        circleMessageEntity.setMessageType(CircleMessageType.CIRCLE_START);
        circleMessageEntity.setMetadataField(CircleMessageMetadataField.message, problemStatement);
        circleMessageEntity.setPosition(timeService.now());

        circleMessageRepository.save(circleMessageEntity);

        CircleDto circleDto = circleMapper.toApi(circleEntity);

        List<CircleMemberDto> memberDtos = new ArrayList<>();
        memberDtos.add(createCircleMember(torchieId));
        circleDto.setMembers(memberDtos);

        activeStatusService.pushWTFStatus(organizationId, torchieId, circleDto.getId(), problemStatement);

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
        circleMessageEntity.setTorchieId(spiritId);
        circleMessageEntity.setMessageType(CircleMessageType.CIRCLE_CLOSED);
        circleMessageEntity.setMetadataField(CircleMessageMetadataField.message, "Circle closed.");
        circleMessageEntity.setPosition(timeService.now());

        circleMessageRepository.save(circleMessageEntity);

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
        circleMessageEntity.setTorchieId(spiritId);
        circleMessageEntity.setMessageType(CircleMessageType.CIRCLE_SHELVED);
        circleMessageEntity.setMetadataField(CircleMessageMetadataField.message, "Circle placed on 'Do It Later' shelf.");
        circleMessageEntity.setPosition(timeService.now());

        circleMessageRepository.save(circleMessageEntity);

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
        circleMessageEntity.setTorchieId(spiritId);
        circleMessageEntity.setMessageType(CircleMessageType.CIRCLE_RESUMED);
        circleMessageEntity.setMetadataField(CircleMessageMetadataField.message, "Circle resumed from 'Do It Later' shelf.");
        circleMessageEntity.setPosition(timeService.now());

        circleMessageRepository.save(circleMessageEntity);

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


    public FeedMessageDto postChatMessageToCircleFeed(UUID organizationId, UUID spiritId, UUID circleId, String chatMessage) {

        CircleMessageEntity circleMessageEntity = new CircleMessageEntity();
        circleMessageEntity.setId(UUID.randomUUID());
        circleMessageEntity.setTorchieId(spiritId);
        circleMessageEntity.setPosition(timeService.now());

        circleMessageEntity.setCircleId(circleId);
        circleMessageEntity.setMetadataField(CircleMessageMetadataField.message, chatMessage);
        circleMessageEntity.setMessageType(CircleMessageType.CHAT);

        circleMessageRepository.save(circleMessageEntity);

        FeedMessageDto feedMessageDto = feedMessageMapper.toApi(circleMessageEntity);
        feedMessageDto.setMessage(circleMessageEntity.getMetadataValue(CircleMessageMetadataField.message));

        feedMessageDto.setCircleMemberDto(createCircleMember(spiritId));
        return feedMessageDto;
    }

    public FeedMessageDto postScreenshotReferenceToCircleFeed(UUID organizationId, UUID spiritId, UUID circleId, ScreenshotReferenceInputDto screenshotReferenceInputDto) {
        CircleMessageEntity circleMessageEntity = new CircleMessageEntity();
        circleMessageEntity.setId(UUID.randomUUID());
        circleMessageEntity.setTorchieId(spiritId);
        circleMessageEntity.setPosition(timeService.now());

        circleMessageEntity.setCircleId(circleId);
        circleMessageEntity.setMetadataField(CircleMessageMetadataField.message, "Added screenshot for " + screenshotReferenceInputDto.getFileName());
        circleMessageEntity.setMetadataField(CircleMessageMetadataField.name, screenshotReferenceInputDto.getFileName());
        circleMessageEntity.setMetadataField(CircleMessageMetadataField.filePath, screenshotReferenceInputDto.getFilePath());
        circleMessageEntity.setMessageType(CircleMessageType.SCREENSHOT);

        circleMessageRepository.save(circleMessageEntity);

        FeedMessageDto feedMessageDto = feedMessageMapper.toApi(circleMessageEntity);

        feedMessageDto.setMessage(circleMessageEntity.getMetadataValue(CircleMessageMetadataField.message));
        feedMessageDto.setFileName(circleMessageEntity.getMetadataValue(CircleMessageMetadataField.name));
        feedMessageDto.setFilePath(circleMessageEntity.getMetadataValue(CircleMessageMetadataField.filePath));

        feedMessageDto.setCircleMemberDto(createCircleMember(spiritId));
        return feedMessageDto;

    }

    CircleDto getCircle(UUID spiritId, UUID circleId) {
        CircleDto circleDto = null;

        CircleEntity circleEntity = circleRepository.findOne(circleId);

        if (circleEntity.getChannelId() == null) {
            circleEntity.setChannelId(UUID.randomUUID());

            circleRepository.save(circleEntity);
        }


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
            circleMemberDto.setSpiritId(circleMember.getTorchieId());
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

        long seconds = startTimer.until(timeService.now(), ChronoUnit.SECONDS);
        seconds += circleDto.getDurationInSeconds();

        return seconds;
    }

    public FeedMessageDto postSnippetToActiveCircleFeed(UUID organizationId, UUID torchieId, NewSnippetEvent snippetEvent) {

        UUID activeCircleId = activeStatusService.getActiveCircleId(organizationId, torchieId);

        FeedMessageDto feedMessageDto = null;

        if (activeCircleId != null) {

            CircleMessageEntity circleMessageEntity = new CircleMessageEntity();
            circleMessageEntity.setId(UUID.randomUUID());
            circleMessageEntity.setTorchieId(torchieId);
            circleMessageEntity.setPosition(timeService.now());

            circleMessageEntity.setCircleId(activeCircleId);
            circleMessageEntity.setMetadataField(CircleMessageMetadataField.message, "Added snippet from " + snippetEvent.getSource());
            circleMessageEntity.setMetadataField(CircleMessageMetadataField.snippetSource, snippetEvent.getSource());
            circleMessageEntity.setMetadataField(CircleMessageMetadataField.snippet, snippetEvent.getSnippet());
            circleMessageEntity.setMessageType(CircleMessageType.SNIPPET);

            circleMessageRepository.save(circleMessageEntity);

            feedMessageDto = feedMessageMapper.toApi(circleMessageEntity);

            feedMessageDto.setMessage(circleMessageEntity.getMetadataValue(CircleMessageMetadataField.message));
            feedMessageDto.setSnippetSource(circleMessageEntity.getMetadataValue(CircleMessageMetadataField.snippetSource));
            feedMessageDto.setSnippet(circleMessageEntity.getMetadataValue(CircleMessageMetadataField.snippet));

            feedMessageDto.setCircleMemberDto(createCircleMember(torchieId));
        }

        return feedMessageDto;

    }


    public List<FeedMessageDto> getAllMessagesForCircleFeed(UUID organizationId, UUID torchieId, UUID circleId) {

        List<CircleFeedMessageEntity> messageEntities = circleFeedWithMembersRepository.findByCircleIdOrderByPosition(circleId);

        List<FeedMessageDto> feedMessageDtos = new ArrayList<>();

        for (CircleFeedMessageEntity messageEntity : messageEntities) {
            FeedMessageDto feedMessageDto = circleFeedMessageMapper.toApi(messageEntity);
            feedMessageDto.setMessage(messageEntity.getMetadataValue(CircleMessageMetadataField.message));
            feedMessageDto.setCircleMemberDto(createCircleMember(torchieId, messageEntity.getFullName()));

            feedMessageDtos.add(feedMessageDto);
        }

        return feedMessageDtos;
    }

    private void validateMemberIsOnTeamOfCircleOwner(UUID organizationId, UUID invokingMemberId, CircleEntity circleEntity) {
        UUID circleOrgId = circleEntity.getOrganizationId();
        UUID ownerMemberId = circleEntity.getOwnerMemberId();

        if (!circleOrgId.equals(organizationId)) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_ORGANIZATION, "Member organization is different than Circle organization");
        }

        teamService.validateMembersOnSameTeam(organizationId, ownerMemberId, invokingMemberId);
    }


    public List<CircleDto> getAllParticipatingCircles(UUID organizationId, UUID torchieId) {
        List<CircleDto> circles = new ArrayList<>();

        List<CircleEntity> circleEntities = circleRepository.findAllByParticipation(organizationId, torchieId);

        for (CircleEntity circleEntity : circleEntities) {
            List<MemberNameEntity> circleMembers = memberNameRepository.findAllByCircleId(circleEntity.getId());

            CircleDto circleDto = this.toCircleDto(circleEntity, circleMembers);
            circles.add(circleDto);

        }

        return circles;
    }


}
