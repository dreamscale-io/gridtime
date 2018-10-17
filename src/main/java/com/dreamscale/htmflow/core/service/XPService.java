package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.activity.*;
import com.dreamscale.htmflow.api.batch.NewBatchEvent;
import com.dreamscale.htmflow.api.batch.NewFlowBatch;
import com.dreamscale.htmflow.api.event.EventType;
import com.dreamscale.htmflow.api.event.NewSnippetEvent;
import com.dreamscale.htmflow.api.status.XPSummaryDto;
import com.dreamscale.htmflow.core.domain.MemberXPEntity;
import com.dreamscale.htmflow.core.domain.MemberXPRepository;
import com.dreamscale.htmflow.core.domain.OrganizationMemberEntity;
import com.dreamscale.htmflow.core.domain.flow.*;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class XPService {

    @Autowired
    OrganizationService organizationService;

    @Autowired
    MemberXPRepository memberXPRepository;


    public void grantXP(UUID organizationId, UUID memberId, int xpAmount) {
        MemberXPEntity memberXPEntity = memberXPRepository.findByMemberId(memberId);

        if (memberXPEntity == null) {
            memberXPEntity = new MemberXPEntity();
            memberXPEntity.setId(UUID.randomUUID());
            memberXPEntity.setOrganizationId(organizationId);
            memberXPEntity.setMemberId(memberId);
            memberXPEntity.setTotalXp(xpAmount);

        } else {
            memberXPEntity.setTotalXp(memberXPEntity.getTotalXp() + xpAmount);
        }

        memberXPRepository.save(memberXPEntity);
    }

    public XPSummaryDto getLatestXPForMember(UUID memberId) {

        MemberXPEntity memberXPEntity = memberXPRepository.findByMemberId(memberId);

        if (memberXPEntity != null) {
            return calculateCurrentXpState(memberXPEntity);
        } else {
            return createDefaultXpSummary();
        }
    }

    private XPSummaryDto calculateCurrentXpState(MemberXPEntity memberXPEntity) {

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

        int currentXp = memberXPEntity.getTotalXp();

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

    @Builder
    private static class Level {
        Integer levelNumber;
        Integer xpRequired;
        String title;
    }
}
