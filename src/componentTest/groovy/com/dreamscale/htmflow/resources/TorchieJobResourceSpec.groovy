package com.dreamscale.htmflow.resources


import com.dreamscale.htmflow.DataTest
import com.dreamscale.htmflow.api.torchie.TorchieJobStatus
import com.dreamscale.htmflow.client.TorchieJobClient
import com.dreamscale.htmflow.core.domain.member.*
import com.dreamscale.htmflow.core.feeds.clock.GeometryClock
import com.dreamscale.htmflow.core.feeds.executor.Torchie
import com.dreamscale.htmflow.core.feeds.executor.TorchieFactory
import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.TileUri
import com.dreamscale.htmflow.core.feeds.story.StoryTile
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
        TorchieJobStatus status = torchieJobClient.startTorchieJobForMember(members.get(0).getId().toString())

        then:
        assert members.size() > 0
        assert status != null;
    }

    def "should run one tile by URI"() {
        given:
        String tileUri = "/torchie/3883f615-9787-4648-b9a8-0a088fb555b7/zoom/TWENTIES/tile/2019_BWD2-2-3_TT5-9";

        when:
        TileUri.SourceCoordinates coords = TileUri.extractCoordinatesFromUri(tileUri);
        System.out.println(coords.getTileCoordinates().formatDreamTime());

        Torchie torchie = torchieExecutorService.findOrCreateMemberTorchie(UUID.fromString("3883f615-9787-4648-b9a8-0a088fb555b7"));
        StoryTile tileOutput = torchie.runTile(tileUri);

        then:
        assert tileOutput != null;

    }


    def "should generate Janelle's feed tiles and click through metronome"() {
        given:
        List<OrganizationMemberEntity> members = memberRepository.findByMasterAccountId(testUser.id)
        torchieExecutorService.startMemberTorchie(members.get(0).getId())

        when:
        torchieExecutorService.runAllTorchies();

        List<TorchieJobStatus> jobStatuses = torchieExecutorService.getAllJobStatus();
        print jobStatuses

        //
        then:
        assert jobStatuses.size() == 1
    }


}
