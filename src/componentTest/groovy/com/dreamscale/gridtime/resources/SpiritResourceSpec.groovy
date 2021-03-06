package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.circuit.LearningCircuitDto
import com.dreamscale.gridtime.api.spirit.ActiveLinksNetworkDto
import com.dreamscale.gridtime.api.spirit.SpiritDto
import com.dreamscale.gridtime.api.spirit.SpiritNetworkDto
import com.dreamscale.gridtime.api.spirit.TombstoneInputDto
import com.dreamscale.gridtime.api.spirit.TorchieTombstoneDto
import com.dreamscale.gridtime.api.spirit.XPDto
import com.dreamscale.gridtime.client.AccountClient
import com.dreamscale.gridtime.client.LearningCircuitClient
import com.dreamscale.gridtime.client.SpiritClient
import com.dreamscale.gridtime.client.TalkToClient
import com.dreamscale.gridtime.core.domain.member.RootAccountEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
import com.dreamscale.gridtime.core.domain.member.SpiritXPEntity
import com.dreamscale.gridtime.core.domain.member.TeamEntity
import com.dreamscale.gridtime.core.domain.member.TeamMemberEntity
import com.dreamscale.gridtime.core.capability.system.GridClock
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class SpiritResourceSpec extends Specification {

    @Autowired
    SpiritClient spiritClient

    @Autowired
    TalkToClient talkToClient;

    @Autowired
    AccountClient accountClient;

    @Autowired
    RootAccountEntity testUser

    @Autowired
    GridClock mockTimeService

    @Autowired
    LearningCircuitClient circuitClient

    OrganizationEntity org


    def setup() {
        mockTimeService.now() >> LocalDateTime.now()

        Map<String, String> keys = new HashMap<>();
        keys.put("discoveryKey", "key1")
        keys.put("key", "key2")
        keys.put("secretKey", "key3")

        org = aRandom.organizationEntity().save()

    }

    def "should get the xp for the Torchie spirit"() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        SpiritXPEntity spiritXPEntity = aRandom.spiritXPEntity().memberId(member.id).save()

        testUser.setId(member.getRootAccountId())

        when:
        SpiritDto spiritDto = spiritClient.getMyTorchie();

        then:
        assert spiritDto != null
        assert spiritDto.xpSummary != null
        assert spiritDto.xpSummary.totalXP == spiritXPEntity.totalXp

    }


    def "should link to another spirit"() {
        given:

        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member1 = aRandom.memberEntity().organizationId(org.id).save()
        OrganizationMemberEntity member2 = aRandom.memberEntity().organizationId(org.id).save()
        OrganizationMemberEntity member3 = aRandom.memberEntity().organizationId(org.id).save()

        SpiritXPEntity spiritXPEntity = aRandom.spiritXPEntity().memberId(member1.id).save()

        testUser.setId(member1.getRootAccountId())

        when:
        ActiveLinksNetworkDto activeLinksNetworkDto1 = spiritClient.linkToTorchie(member2.getId().toString())
        ActiveLinksNetworkDto activeLinksNetworkDto2 = spiritClient.linkToTorchie(member3.getId().toString())

        then:
        assert activeLinksNetworkDto1 != null
        assert activeLinksNetworkDto1.networkId != null
        assert activeLinksNetworkDto1.getSpiritLinks().size() == 1

        assert activeLinksNetworkDto2 != null
        assert activeLinksNetworkDto1.networkId == activeLinksNetworkDto2.networkId
        assert activeLinksNetworkDto2.getSpiritLinks().size() == 2

    }

    def "should link to another 2 spirits then unlink 1 spirit"() {
        given:

        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member1 = aRandom.memberEntity().organizationId(org.id).save()
        OrganizationMemberEntity member2 = aRandom.memberEntity().organizationId(org.id).save()
        OrganizationMemberEntity member3 = aRandom.memberEntity().organizationId(org.id).save()

        SpiritXPEntity spiritXPEntity = aRandom.spiritXPEntity().memberId(member1.id).save()

        testUser.setId(member1.getRootAccountId())

        when:
        ActiveLinksNetworkDto activeLinksNetworkDto1 = spiritClient.linkToTorchie(member2.getId().toString())
        ActiveLinksNetworkDto activeLinksNetworkDto2 = spiritClient.linkToTorchie(member3.getId().toString())
        ActiveLinksNetworkDto activeLinksNetworkDto3 = spiritClient.unlinkTorchie(member2.getId().toString())

        then:

        assert activeLinksNetworkDto2.networkId == activeLinksNetworkDto3.networkId
        assert activeLinksNetworkDto3.getSpiritLinks().size() == 1

    }

    def "should link to another spirit then delete the network"() {
        given:

        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member1 = aRandom.memberEntity().organizationId(org.id).save()
        OrganizationMemberEntity member2 = aRandom.memberEntity().organizationId(org.id).save()

        SpiritXPEntity spiritXPEntity = aRandom.spiritXPEntity().memberId(member1.id).save()

        testUser.setId(member1.getRootAccountId())

        when:
        ActiveLinksNetworkDto activeLinksNetworkDto1 = spiritClient.linkToTorchie(member2.getId().toString())
        ActiveLinksNetworkDto activeLinksNetworkDto2 = spiritClient.unlinkTorchie(member2.getId().toString())

        then:

        assert activeLinksNetworkDto2 != null
        assert activeLinksNetworkDto2.getSpiritLinks().size() == 0

    }

    def "should unlink me and preserve network of spirit friends"() {
        given:

        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member1 = aRandom.memberEntity().organizationId(org.id).save()
        OrganizationMemberEntity member2 = aRandom.memberEntity().organizationId(org.id).save()
        OrganizationMemberEntity member3 = aRandom.memberEntity().organizationId(org.id).save()

        SpiritXPEntity spiritXPEntity = aRandom.spiritXPEntity().memberId(member1.id).save()

        testUser.setId(member1.getRootAccountId())

        ActiveLinksNetworkDto activeLinksNetworkDto1 = spiritClient.linkToTorchie(member2.getId().toString())
        ActiveLinksNetworkDto activeLinksNetworkDto2 = spiritClient.linkToTorchie(member3.getId().toString())

        when:
        spiritClient.unlinkMe()

        SpiritDto mySpirit = spiritClient.getMyTorchie();
        SpiritDto friendSpirit = spiritClient.getFriendTorchie(member3.getId().toString());

        then:
        assert mySpirit != null
        assert mySpirit.activeSpiritLinks.spiritLinks.size() == 0

        assert friendSpirit != null
        assert friendSpirit.activeSpiritLinks.spiritLinks.size() == 1

    }

    def "RIP torchie should generate tombstones and reset XP"() {
        given:

        RootAccountEntity masterAccountEntity = aRandom.rootAccountEntity().save();
        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member1 = aRandom.memberEntity()
                .rootAccountId(masterAccountEntity.id).organizationId(org.id).save()

        SpiritXPEntity spiritXPEntity = aRandom.spiritXPEntity().memberId(member1.id).save()

        testUser.setId(member1.getRootAccountId())
        TombstoneInputDto inputDto = new TombstoneInputDto(epitaph: "Here lies Torchie");

        when:
        TorchieTombstoneDto tombstoneDto = spiritClient.restInPeace(inputDto);
        SpiritDto mySpirit = spiritClient.getMyTorchie();

        List<TorchieTombstoneDto> tombstones = spiritClient.getMyTombstones();

        then:
        assert tombstoneDto != null
        assert tombstoneDto.totalXp == spiritXPEntity.totalXp
        assert tombstoneDto.epitaph == inputDto.epitaph

        assert mySpirit != null
        assert mySpirit.xpSummary.totalXP == 0
        assert tombstones.size() == 1

    }

    def "should get my network of links and circles"() {
        given:

        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member1 = aRandom.memberEntity().organizationId(org.id).save()
        OrganizationMemberEntity member2 = aRandom.memberEntity().organizationId(org.id).save()
        OrganizationMemberEntity member3 = aRandom.memberEntity().organizationId(org.id).save()

        SpiritXPEntity spiritXPEntity = aRandom.spiritXPEntity().memberId(member1.id).save()

        testUser.setId(member1.getRootAccountId())

        ActiveLinksNetworkDto activeLinksNetworkDto1 = spiritClient.linkToTorchie(member2.getId().toString())
        ActiveLinksNetworkDto activeLinksNetworkDto2 = spiritClient.linkToTorchie(member3.getId().toString())

        when:
        SpiritNetworkDto spiritNetworkDto = spiritClient.getMySpiritNetwork();
        SpiritNetworkDto friendSpiritNetworkDto = spiritClient.getFriendSpiritNetwork(member2.getId().toString())


        then:
        assert spiritNetworkDto != null
        assert spiritNetworkDto.activeLinksNetwork.spiritLinks.size() == 2

        assert friendSpiritNetworkDto.activeLinksNetwork.spiritLinks.size() == 2

    }

    private OrganizationMemberEntity createMemberWithOrgAndTeam() {

        RootAccountEntity account = aRandom.rootAccountEntity().save()

        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).rootAccountId(account.id).save()
        TeamEntity team = aRandom.teamEntity().organizationId(org.id).save()
        TeamMemberEntity teamMember = aRandom.teamMemberEntity().teamId(team.id).organizationId(org.id).memberId(member.id).save()

        return member;

    }
}
