package com.dreamscale.htmflow.resources


import com.dreamscale.htmflow.DataTest
import com.dreamscale.htmflow.api.torchie.TorchieJobStatus
import com.dreamscale.htmflow.client.TorchieJobClient
import com.dreamscale.htmflow.core.domain.member.*
import com.dreamscale.htmflow.core.service.TorchieExecutorService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

@DataTest
class TorchieJobResourceSpec extends Specification {

    @Autowired
    TorchieJobClient torchieJobClient

    @Autowired
    OrganizationMemberRepository memberRepository

    @Autowired
    TorchieExecutorService torchieExecutorService

    @Autowired
    MasterAccountEntity testUser

    def setup() {
        testUser.setId(UUID.fromString("bd17b473-1a06-47fe-8e5e-6be03bd041cf"))
        testUser.setApiKey("f7c3226ffb704bda987b3d7daf327a8a")

    }


    def "should query prod data"() {
        given:
        List<OrganizationMemberEntity> members = memberRepository.findByMasterAccountId(testUser.id)

        when:
        TorchieJobStatus status = torchieJobClient.startTorchieJobForMember(members.get(0).getId().toString())

        then:
        assert members.size() > 0
        assert status != null;
    }

    def "should generate Janelle's feed tiles and click through metronome"() {
        given:
        List<OrganizationMemberEntity> members = memberRepository.findByMasterAccountId(testUser.id)
        torchieExecutorService.startMemberTorchie(members.get(0).getId())

        when:
        torchieExecutorService.runAllTorchies();

        List<TorchieJobStatus> jobStatuses = torchieExecutorService.getAllJobStatus();
        print jobStatuses

        then:
        assert jobStatuses.size() == 1
    }
    

}
