package com.dreamscale.htmflow.resources


import com.dreamscale.htmflow.DataTest
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.circuit.CircuitMonitor

import com.dreamscale.htmflow.core.domain.member.*
import com.dreamscale.htmflow.core.gridtime.executor.machine.Torchie
import com.dreamscale.htmflow.core.gridtime.executor.machine.TorchieFactory
import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.GridTile
import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.TileUri

import com.dreamscale.htmflow.core.gridtime.executor.machine.TorchiePoolExecutor
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

@DataTest
class TorchieJobResourceSpec extends Specification {


    @Autowired
    OrganizationMemberRepository memberRepository

    @Autowired
    TorchiePoolExecutor torchieExecutor

    @Autowired
    TorchieFactory torchieFactory

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
        CircuitMonitor status = torchieJobClient.startTorchieJobForMember(members.get(0).getId().toString())

        then:
        assert members.size() > 0
        assert status != null;
    }

    def "should run one tile by URI"() {
        given:
        String tileUri = "/torchie/3883f615-9787-4648-b9a8-0a088fb555b7/zoom/TWENTY/tile/2019_BWD2-2-3_TT5-9";

        when:
        TileUri.SourceCoordinates coords = TileUri.extractCoordinatesFromUri(tileUri);
        System.out.println(coords.getTileCoordinates().formatGridTime());

        Torchie torchie = torchieExecutor.findOrCreateMemberTorchie(UUID.fromString("3883f615-9787-4648-b9a8-0a088fb555b7"));
        GridTile tileOutput = torchie.runTile(tileUri);

        then:
        assert tileOutput != null;

    }


    def "should generate Janelle's feed tiles and click through metronome"() {
        given:
        List<OrganizationMemberEntity> members = memberRepository.findByMasterAccountId(testUser.id)
        OrganizationMemberEntity member = members.get(0);
        torchieExecutor.startMemberTorchie(member.getId())

        when:
        torchieExecutor.runAllTorchies();

        List<CircuitMonitor> jobStatuses = torchieExecutor.getAllTorchieMonitors();
        print jobStatuses

        //
        then:
        assert jobStatuses.size() == 1
    }


}
