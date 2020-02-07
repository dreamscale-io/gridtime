package com.dreamscale.gridtime.core.service;

import com.dreamscale.gridtime.api.spirit.*;
import com.dreamscale.gridtime.core.domain.active.ActiveSpiritLinkEntity;
import com.dreamscale.gridtime.core.domain.active.ActiveSpiritLinkRepository;
import com.dreamscale.gridtime.core.domain.member.*;
import com.dreamscale.gridtime.core.mapper.DtoEntityMapper;
import com.dreamscale.gridtime.core.mapper.MapperFactory;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class SpiritService {

    @Autowired
    OrganizationService organizationService;

    @Autowired
    AccountService accountService;

    @Autowired
    CircuitOperator circuitOperator;

    @Autowired
    ActiveSpiritLinkRepository activeSpiritLinkRepository;

    @Autowired
    TorchieTombstoneRepository torchieTombstoneRepository;

    @Autowired
    SpiritXPRepository spiritXPRepository;

    @Autowired
    MemberDetailsService memberDetailsService;

    @Autowired
    TimeService timeService;

    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<TorchieTombstoneDto, TorchieTombstoneEntity> torchieTombstoneMapper;

    @PostConstruct
    private void init() {
        torchieTombstoneMapper = mapperFactory.createDtoEntityMapper(TorchieTombstoneDto.class, TorchieTombstoneEntity.class);
    }


    public SpiritDto getTorchie(UUID organizationId, UUID torchieId) {
        SpiritDto spiritDto = new SpiritDto();
        spiritDto.setSpiritId(torchieId);
        spiritDto.setXpSummary(this.getLatestXPForSpirit(torchieId));
        spiritDto.setActiveSpiritLinks(this.getActiveLinksNetwork(organizationId, torchieId));

        return spiritDto;
    }


    public ActiveLinksNetworkDto linkToTorchie(UUID organizationId, UUID invokingTorchie, UUID friendTorchie) {

        ActiveLinksNetworkDto spiritNetwork = this.getActiveLinksNetwork(organizationId, invokingTorchie);

        if (!networkContainsLink(spiritNetwork, invokingTorchie)) {
            ActiveSpiritLinkEntity meLink = new ActiveSpiritLinkEntity();
            meLink.setId(UUID.randomUUID());
            meLink.setNetworkId(spiritNetwork.getNetworkId());
            meLink.setTorchieId(invokingTorchie);

            activeSpiritLinkRepository.save(meLink);
        }

        if (!networkContainsLink(spiritNetwork, friendTorchie)) {
            ActiveSpiritLinkEntity friendTorchieLink = new ActiveSpiritLinkEntity();
            friendTorchieLink.setId(UUID.randomUUID());
            friendTorchieLink.setNetworkId(spiritNetwork.getNetworkId());
            friendTorchieLink.setTorchieId(friendTorchie);

            activeSpiritLinkRepository.save(friendTorchieLink);

            SpiritLinkDto spiritLinkDto = new SpiritLinkDto();
            spiritLinkDto.setSpiritId(invokingTorchie);
            spiritLinkDto.setFriendSpiritId(friendTorchie);
            spiritLinkDto.setName(memberDetailsService.lookupMemberName(organizationId, friendTorchie));
            spiritNetwork.addSpiritLink(spiritLinkDto);
        }

        return spiritNetwork;
    }

    public ActiveLinksNetworkDto unlinkTorchie(UUID organizationId, UUID invokingTorchieId, UUID friendTorchieId) {

        ActiveLinksNetworkDto spiritNetwork = this.getActiveLinksNetwork(organizationId, invokingTorchieId);

        if (networkContainsLink(spiritNetwork, friendTorchieId)) {

            this.deleteLinks(spiritNetwork, friendTorchieId);
        }

        return this.getActiveLinksNetwork(organizationId, invokingTorchieId);
    }


    public ActiveLinksNetworkDto getActiveLinksNetwork(UUID organizationId, UUID memberId) {

        List<ActiveSpiritLinkEntity> torchieLinks = activeSpiritLinkRepository.findMySpiritNetwork(memberId);

        ActiveLinksNetworkDto activeLinksNetworkDto = new ActiveLinksNetworkDto();

        if (torchieLinks.size() > 0) {
            for (ActiveSpiritLinkEntity torchieLink : torchieLinks) {
                activeLinksNetworkDto.setNetworkId(torchieLink.getNetworkId());

                if (!torchieLink.getTorchieId().equals(memberId)) {
                    SpiritLinkDto spiritLinkDto = new SpiritLinkDto();
                    spiritLinkDto.setSpiritId(memberId);
                    spiritLinkDto.setFriendSpiritId(torchieLink.getTorchieId());
                    spiritLinkDto.setName(memberDetailsService.lookupMemberName(organizationId, torchieLink.getTorchieId()));
                    activeLinksNetworkDto.addSpiritLink(spiritLinkDto);
                }
            }
        } else {
            activeLinksNetworkDto.setNetworkId(UUID.randomUUID());
        }

        activeLinksNetworkDto.setMyId(memberId);
        activeLinksNetworkDto.setMyName(memberDetailsService.lookupMemberName(organizationId, memberId));

        return activeLinksNetworkDto;
    }


    public void unlinkMe(UUID organizationId, UUID spiritId) {
        ActiveLinksNetworkDto spiritNetwork = this.getActiveLinksNetwork(organizationId, spiritId);

        this.deleteLinks(spiritNetwork, spiritId);
    }

    private void deleteLinks(ActiveLinksNetworkDto spiritNetwork, UUID torchieId) {
        if (spiritNetwork.containsOneLink()) {
            List<ActiveSpiritLinkEntity> links = activeSpiritLinkRepository.findByNetworkId(spiritNetwork.getNetworkId());
            for (ActiveSpiritLinkEntity link : links) {
                activeSpiritLinkRepository.delete(link);
            }
        } else {
            ActiveSpiritLinkEntity link =
                    activeSpiritLinkRepository.findByNetworkIdAndTorchieId(spiritNetwork.getNetworkId(), torchieId);
            activeSpiritLinkRepository.delete(link);
        }
    }


    public SpiritNetworkDto getSpiritNetwork(UUID organizationId, UUID torchieId) {

        SpiritNetworkDto spiritNetworkDto = new SpiritNetworkDto();
        spiritNetworkDto.setActiveLinksNetwork(this.getActiveLinksNetwork(organizationId, torchieId));
        spiritNetworkDto.setActiveCircles(circuitOperator.getAllParticipatingCircuits(organizationId, torchieId));

        return spiritNetworkDto;
    }

    public TorchieTombstoneDto restInPeace(UUID masterAccountId, UUID organizationId, UUID torchieId, String epitaph) {
        XPSummaryDto xpSummaryDto = this.getLatestXPForSpirit(torchieId);

        TorchieTombstoneEntity torchieTombstoneEntity = new TorchieTombstoneEntity();
        torchieTombstoneEntity.setId(UUID.randomUUID());
        torchieTombstoneEntity.setTorchieId(torchieId);
        torchieTombstoneEntity.setLevel(xpSummaryDto.getLevel());
        torchieTombstoneEntity.setTitle(xpSummaryDto.getTitle());
        torchieTombstoneEntity.setTotalXp(xpSummaryDto.getTotalXP());
        torchieTombstoneEntity.setEpitaph(epitaph);
        torchieTombstoneEntity.setDateOfDeath(timeService.now());
        torchieTombstoneEntity.setDateOfBirth(lookupTorchieBirthday(masterAccountId, torchieId));

        torchieTombstoneRepository.save(torchieTombstoneEntity);

        SpiritXPEntity spiritXp = spiritXPRepository.findByMemberId(torchieId);
        spiritXp.setTotalXp(0);

        spiritXPRepository.save(spiritXp);

        return torchieTombstoneMapper.toApi(torchieTombstoneEntity);
    }

    public List<TorchieTombstoneDto> getMyTombstones(UUID organizationId, UUID spiritId) {
        List<TorchieTombstoneEntity> tombstoneEntities = torchieTombstoneRepository.findByTorchieIdOrderByDateOfDeath(spiritId);
        return torchieTombstoneMapper.toApiList(tombstoneEntities);
    }

    private LocalDateTime lookupTorchieBirthday(UUID masterAccountId, UUID spiritId) {
        LocalDateTime birthday = null;

        TorchieTombstoneEntity lastTombstone = torchieTombstoneRepository.findLatestByTorchieId(spiritId);

        if (lastTombstone != null) {
            birthday = lastTombstone.getDateOfDeath();
        } else {
            birthday = accountService.getActivationDate(masterAccountId);
        }
        return birthday;
    }


    private boolean networkContainsLink(ActiveLinksNetworkDto spiritNetwork, UUID spiritId) {
        boolean containsLink = false;

        for (SpiritLinkDto link : spiritNetwork.getSpiritLinks()) {
            if (link.getSpiritId().equals(spiritId) ||
                    link.getFriendSpiritId().equals(spiritId)) {
                containsLink = true;
                break;
            }
        }


        return containsLink;
    }

    private UUID findOrCreateNetworkId(ActiveLinksNetworkDto spiritNetwork) {
        UUID networkId;

        if (spiritNetwork != null) {
            networkId = spiritNetwork.getNetworkId();
        } else {
            networkId = UUID.randomUUID();
        }
        return networkId;
    }


    public void grantXP(UUID organizationId, UUID torchieId, int xpAmount) {
        SpiritXPEntity spiritXPEntity = spiritXPRepository.findByMemberId(torchieId);

        if (spiritXPEntity == null) {
            spiritXPEntity = new SpiritXPEntity();
            spiritXPEntity.setId(UUID.randomUUID());
            spiritXPEntity.setOrganizationId(organizationId);
            spiritXPEntity.setMemberId(torchieId);
            spiritXPEntity.setTotalXp(xpAmount);

        } else {
            spiritXPEntity.setTotalXp(spiritXPEntity.getTotalXp() + xpAmount);
        }

        spiritXPRepository.save(spiritXPEntity);
    }

    private XPSummaryDto getLatestXPForSpirit(UUID torchidId) {

        SpiritXPEntity memberXPEntity = spiritXPRepository.findByMemberId(torchidId);

        if (memberXPEntity != null) {
            return calculateCurrentXpState(memberXPEntity.getTotalXp());
        } else {
            return createDefaultXpSummary();
        }
    }

    private XPSummaryDto calculateCurrentXpState(Integer totalXp) {

        Map<Integer, Level> levels = new TreeMap<>();

        levels.put(1, Level.builder().levelNumber(1).title("n00b").xpRequired(0).build());
        levels.put(2, Level.builder().levelNumber(2).title("Apprentice").xpRequired(50).build());
        levels.put(3, Level.builder().levelNumber(3).title("Apprentice").xpRequired(80).build());
        levels.put(4, Level.builder().levelNumber(4).title("Journeyspirit").xpRequired(150).build());
        levels.put(5, Level.builder().levelNumber(5).title("Journeyspirit").xpRequired(250).build());
        levels.put(6, Level.builder().levelNumber(6).title("Journeyspirit").xpRequired(310).build());
        levels.put(7, Level.builder().levelNumber(7).title("Journeyspirit").xpRequired(520).build());
        levels.put(8, Level.builder().levelNumber(8).title("Peer Mentor").xpRequired(770).build());
        levels.put(9, Level.builder().levelNumber(9).title("Peer Mentor").xpRequired(1020).build());
        levels.put(10, Level.builder().levelNumber(10).title("Peer Mentor").xpRequired(1310).build());

        int currentXp = totalXp;

        int xpRequired = 0;
        Level currentLevel = levels.get(1);
        int unspentXp = currentXp;

        for (Map.Entry<Integer, Level> xpEntry : levels.entrySet()) {

            Level level = xpEntry.getValue();
            xpRequired += level.xpRequired;

            if (currentXp >= xpRequired) {
                currentLevel = level;
                unspentXp -= level.xpRequired;
            }
        }

        XPSummaryDto xpSummaryDto = new XPSummaryDto();
        xpSummaryDto.setLevel(currentLevel.levelNumber);
        xpSummaryDto.setTitle(currentLevel.title);
        xpSummaryDto.setTotalXP(currentXp);

        if (currentLevel.levelNumber < 10) {
            xpSummaryDto.setXpRequiredToLevel(levels.get(currentLevel.levelNumber + 1).xpRequired);
            xpSummaryDto.setXpProgress(unspentXp);
        }
        return xpSummaryDto;
    }

    private XPSummaryDto createDefaultXpSummary() {
        XPSummaryDto xpSummary = new XPSummaryDto();

        xpSummary.setLevel(1);
        xpSummary.setTotalXP(0);
        xpSummary.setXpProgress(0);
        xpSummary.setXpRequiredToLevel(50);
        xpSummary.setTitle("n00b");
        return xpSummary;
    }

    public XPSummaryDto translateToXPSummary(Integer totalXp) {
        if (totalXp != null) {
            return calculateCurrentXpState(totalXp);
        } else {
            return createDefaultXpSummary();
        }

    }


    @Builder
    private static class Level {
        Integer levelNumber;
        Integer xpRequired;
        String title;
    }
}
