package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.spirit.*;
import com.dreamscale.htmflow.core.domain.*;
import com.dreamscale.htmflow.core.mapper.DtoEntityMapper;
import com.dreamscale.htmflow.core.mapper.MapperFactory;
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
    CircleService circleService;

    @Autowired
    SpiritNetworkEventRepository spiritNetworkEventRepository;

    @Autowired
    ActiveSpiritLinkRepository activeSpiritLinkRepository;

    @Autowired
    TorchieTombstoneRepository torchieTombstoneRepository;

    @Autowired
    SpiritXPRepository spiritXPRepository;

    @Autowired
    TimeService timeService;

    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<TorchieTombstoneDto, TorchieTombstoneEntity> torchieTombstoneMapper;

    @PostConstruct
    private void init() {
        torchieTombstoneMapper = mapperFactory.createDtoEntityMapper(TorchieTombstoneDto.class, TorchieTombstoneEntity.class);
    }


    public SpiritDto getSpirit(UUID organizationId, UUID spiritId) {
        SpiritDto spiritDto = new SpiritDto();
        spiritDto.setSpiritId(spiritId);
        spiritDto.setXpSummary(this.getLatestXPForSpirit(spiritId));
        spiritDto.setActiveSpiritLinks(this.getActiveLinksNetwork(organizationId, spiritId));

        return spiritDto;
    }


    public ActiveLinksNetworkDto linkToSpirit(UUID organizationId, UUID invokingSpirit, UUID friendSpirit) {

        ActiveLinksNetworkDto spiritNetwork = this.getActiveLinksNetwork(organizationId, invokingSpirit);

        if (!networkContainsLink(spiritNetwork, invokingSpirit)) {
            ActiveSpiritLinkEntity meSpiritEntity = new ActiveSpiritLinkEntity();
            meSpiritEntity.setId(UUID.randomUUID());
            meSpiritEntity.setNetworkId(spiritNetwork.getNetworkId());
            meSpiritEntity.setSpiritId(invokingSpirit);

            activeSpiritLinkRepository.save(meSpiritEntity);
        }

        if (!networkContainsLink(spiritNetwork, friendSpirit)) {
            ActiveSpiritLinkEntity friendSpiritEntity = new ActiveSpiritLinkEntity();
            friendSpiritEntity.setId(UUID.randomUUID());
            friendSpiritEntity.setNetworkId(spiritNetwork.getNetworkId());
            friendSpiritEntity.setSpiritId(friendSpirit);

            activeSpiritLinkRepository.save(friendSpiritEntity);

            SpiritLinkDto spiritLinkDto = new SpiritLinkDto();
            spiritLinkDto.setSpiritId(invokingSpirit);
            spiritLinkDto.setFriendSpiritId(friendSpirit);
            spiritNetwork.addSpiritLink(spiritLinkDto);
        }

        return spiritNetwork;
    }

    public ActiveLinksNetworkDto unlinkSpirit(UUID organizationId, UUID invokingSpiritId, UUID friendSpiritId) {

        ActiveLinksNetworkDto spiritNetwork = this.getActiveLinksNetwork(organizationId, invokingSpiritId);

        if (networkContainsLink(spiritNetwork, friendSpiritId)) {

            this.deleteLinks(spiritNetwork, friendSpiritId);
        }

        return this.getActiveLinksNetwork(organizationId, invokingSpiritId);
    }


    public ActiveLinksNetworkDto getActiveLinksNetwork(UUID organizationId, UUID spiritId) {

        List<ActiveSpiritLinkEntity> spiritLinks = activeSpiritLinkRepository.findMySpiritNetwork(spiritId);

        ActiveLinksNetworkDto activeLinksNetworkDto = new ActiveLinksNetworkDto();

        if (spiritLinks.size() > 0) {
            for (ActiveSpiritLinkEntity spiritLinkEntity : spiritLinks) {
                activeLinksNetworkDto.setNetworkId(spiritLinkEntity.getNetworkId());

                if (!spiritLinkEntity.getSpiritId().equals(spiritId)) {
                    SpiritLinkDto spiritLinkDto = new SpiritLinkDto();
                    spiritLinkDto.setSpiritId(spiritId);
                    spiritLinkDto.setFriendSpiritId(spiritLinkEntity.getSpiritId());

                    activeLinksNetworkDto.addSpiritLink(spiritLinkDto);
                }
            }
        } else {
            activeLinksNetworkDto.setNetworkId(UUID.randomUUID());
        }

        return activeLinksNetworkDto;
    }

    public void unlinkMe(UUID organizationId, UUID spiritId) {
        ActiveLinksNetworkDto spiritNetwork = this.getActiveLinksNetwork(organizationId, spiritId);

        this.deleteLinks(spiritNetwork, spiritId);
    }

    private void deleteLinks(ActiveLinksNetworkDto spiritNetwork, UUID spiritId) {
        if (spiritNetwork.containsOneLink()) {
            List<ActiveSpiritLinkEntity> links = activeSpiritLinkRepository.findByNetworkId(spiritNetwork.getNetworkId());
            for (ActiveSpiritLinkEntity link : links) {
                activeSpiritLinkRepository.delete(link);
            }
        } else {
            ActiveSpiritLinkEntity link =
                    activeSpiritLinkRepository.findByNetworkIdAndSpiritId(spiritNetwork.getNetworkId(), spiritId);
            activeSpiritLinkRepository.delete(link);
        }
    }


    public SpiritNetworkDto getSpiritNetwork(UUID organizationId, UUID spiritId) {

        SpiritNetworkDto spiritNetworkDto = new SpiritNetworkDto();
        spiritNetworkDto.setActiveLinksNetwork(this.getActiveLinksNetwork(organizationId, spiritId));
        spiritNetworkDto.setActiveCircles(circleService.getAllParticipatingCircles(organizationId, spiritId));

        return spiritNetworkDto;
    }

    public TorchieTombstoneDto restInPeace(UUID masterAccountId, UUID organizationId, UUID spiritId, String epitaph) {
        XPSummaryDto xpSummaryDto = this.getLatestXPForSpirit(spiritId);

        TorchieTombstoneEntity torchieTombstoneEntity = new TorchieTombstoneEntity();
        torchieTombstoneEntity.setId(UUID.randomUUID());
        torchieTombstoneEntity.setSpiritId(spiritId);
        torchieTombstoneEntity.setLevel(xpSummaryDto.getLevel());
        torchieTombstoneEntity.setTitle(xpSummaryDto.getTitle());
        torchieTombstoneEntity.setTotalXp(xpSummaryDto.getTotalXP());
        torchieTombstoneEntity.setEpitaph(epitaph);
        torchieTombstoneEntity.setDateOfDeath(timeService.now());
        torchieTombstoneEntity.setDateOfBirth(lookupTorchieBirthday(masterAccountId, spiritId));

        torchieTombstoneRepository.save(torchieTombstoneEntity);

        SpiritXPEntity spiritXp = spiritXPRepository.findBySpiritId(spiritId);
        spiritXp.setTotalXp(0);

        spiritXPRepository.save(spiritXp);

        return torchieTombstoneMapper.toApi(torchieTombstoneEntity);
    }

    public List<TorchieTombstoneDto> getMyTombstones(UUID organizationId, UUID spiritId) {
        List<TorchieTombstoneEntity> tombstoneEntities = torchieTombstoneRepository.findBySpiritIdOrderByDateOfDeath(spiritId);
        return torchieTombstoneMapper.toApiList(tombstoneEntities);
    }

    private LocalDateTime lookupTorchieBirthday(UUID masterAccountId, UUID spiritId) {
        LocalDateTime birthday = null;

        TorchieTombstoneEntity lastTombstone = torchieTombstoneRepository.findLatestBySpiritId(spiritId);

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


    public void grantXP(UUID organizationId, UUID spiritId, int xpAmount) {
        SpiritXPEntity spiritXPEntity = spiritXPRepository.findBySpiritId(spiritId);

        if (spiritXPEntity == null) {
            spiritXPEntity = new SpiritXPEntity();
            spiritXPEntity.setId(UUID.randomUUID());
            spiritXPEntity.setOrganizationId(organizationId);
            spiritXPEntity.setSpiritId(spiritId);
            spiritXPEntity.setTotalXp(xpAmount);

        } else {
            spiritXPEntity.setTotalXp(spiritXPEntity.getTotalXp() + xpAmount);
        }

        spiritXPRepository.save(spiritXPEntity);
    }

    private XPSummaryDto getLatestXPForSpirit(UUID spiritId) {

        SpiritXPEntity memberXPEntity = spiritXPRepository.findBySpiritId(spiritId);

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
