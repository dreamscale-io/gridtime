package com.dreamscale.htmflow.core.gridtime.executor.memory.grid.track

import com.dreamscale.htmflow.core.gridtime.executor.clock.MusicClock
import com.dreamscale.htmflow.core.gridtime.executor.clock.ZoomLevel
import com.dreamscale.htmflow.core.gridtime.executor.memory.FeatureCache
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.reference.IdeaFlowStateReference
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.FeatureTag
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.types.StartTypeTag
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.type.IdeaFlowStateType
import spock.lang.Specification

class BandedMusicTrackSpec extends Specification {


    BandedMusicTrack<IdeaFlowStateReference> bandedMusicTrack
    MusicClock musicClock
    FeatureCache featureCache

    def setup() {

        featureCache = new FeatureCache();
        musicClock = new MusicClock(ZoomLevel.TWENTY)
        bandedMusicTrack = new BandedMusicTrack<>(musicClock)

        featureCache
    }

    def "should generate band that goes from startPlaying(beat) and rollover to the end"() {
        given:
        IdeaFlowStateReference wtfState = featureCache.lookupIdeaFlowStateReference(IdeaFlowStateType.WTF_STATE)

        when:
        bandedMusicTrack.startPlaying(musicClock.getBeat(5), wtfState)
        FeatureTag<IdeaFlowStateReference> rolloverTag = bandedMusicTrack.finish()

        then:
        assert wtfState == bandedMusicTrack.getFeatureAt(5)
        assert wtfState == bandedMusicTrack.getFeatureAt(6)
        assert wtfState == bandedMusicTrack.getFeatureAt(7)
        assert wtfState == bandedMusicTrack.getFeatureAt(8)
        assert wtfState == bandedMusicTrack.getFeatureAt(9)
        assert wtfState == bandedMusicTrack.getFeatureAt(10)
        assert wtfState == bandedMusicTrack.getFeatureAt(20)

        assert rolloverTag != null
        assert rolloverTag.tag == StartTypeTag.Rollover
        assert rolloverTag.feature == wtfState

    }

    def "should generate band that goes from startPlaying(beat) and switch on state switch"() {
        given:
        IdeaFlowStateReference wtfState = featureCache.lookupIdeaFlowStateReference(IdeaFlowStateType.WTF_STATE)
        IdeaFlowStateReference progressState = featureCache.lookupIdeaFlowStateReference(IdeaFlowStateType.PROGRESS_STATE)

        when:
        bandedMusicTrack.startPlaying(musicClock.getBeat(5), wtfState)
        bandedMusicTrack.startPlaying(musicClock.getBeat(9), progressState)

        FeatureTag<IdeaFlowStateReference> rolloverTag = bandedMusicTrack.finish()

        then:
        assert wtfState == bandedMusicTrack.getFeatureAt(5)
        assert wtfState == bandedMusicTrack.getFeatureAt(6)
        assert wtfState == bandedMusicTrack.getFeatureAt(7)
        assert wtfState == bandedMusicTrack.getFeatureAt(8)
        assert progressState == bandedMusicTrack.getFeatureAt(9)
        assert progressState == bandedMusicTrack.getFeatureAt(10)
        assert progressState == bandedMusicTrack.getFeatureAt(20)

        assert rolloverTag != null
        assert rolloverTag.tag == StartTypeTag.Rollover
        assert rolloverTag.feature == progressState

    }

    def "should generate band that goes from startPlaying(beat) unit point of clearing"() {
        given:
        IdeaFlowStateReference wtfState = featureCache.lookupIdeaFlowStateReference(IdeaFlowStateType.WTF_STATE)

        when:
        bandedMusicTrack.startPlaying(musicClock.getBeat(5), wtfState)
        bandedMusicTrack.stopPlaying(musicClock.gotoBeat(7))

        FeatureTag<IdeaFlowStateReference> rolloverTag = bandedMusicTrack.finish()

        then:
        assert wtfState == bandedMusicTrack.getFeatureAt(5)
        assert wtfState == bandedMusicTrack.getFeatureAt(6)
        assert wtfState == bandedMusicTrack.getFeatureAt(7)
        assert null == bandedMusicTrack.getFeatureAt(8)

        assert rolloverTag == null

    }


}
