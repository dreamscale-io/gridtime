package com.dreamscale.gridtime.core.machine.memory.grid.track

import com.dreamscale.gridtime.core.machine.clock.GeometryClock
import com.dreamscale.gridtime.core.machine.clock.MusicClock
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache
import com.dreamscale.gridtime.core.machine.memory.feature.reference.IdeaFlowStateReference
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.FeatureRowKey
import com.dreamscale.gridtime.core.machine.memory.tag.FeatureTag
import com.dreamscale.gridtime.core.machine.memory.tag.types.StartTypeTag
import com.dreamscale.gridtime.core.machine.memory.type.IdeaFlowStateType
import spock.lang.Specification

import java.time.LocalDateTime

class BandedMusicTrackSpec extends Specification {


    BandedMusicTrack<IdeaFlowStateReference> bandedMusicTrack
    MusicClock musicClock
    FeatureCache featureCache
    GeometryClock.GridTime gridTime

    def setup() {

        featureCache = new FeatureCache();
        gridTime = new GeometryClock(LocalDateTime.now()).getActiveGridTime()
        musicClock = new MusicClock(ZoomLevel.TWENTY)
        bandedMusicTrack = new BandedMusicTrack<>(FeatureRowKey.EXEC_RHYTHM, gridTime, musicClock)

        featureCache
    }

    def "should generate band that goes from startPlaying(beat) and rollover to the end"() {
        given:
        IdeaFlowStateReference wtfState = featureCache.lookupIdeaFlowStateReference(IdeaFlowStateType.WTF_STATE)

        LocalDateTime moment = gridTime.getMomentFromOffset(musicClock.getBeat(5).getRelativeDuration());

        when:
        bandedMusicTrack.startPlaying(moment, wtfState)
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

        LocalDateTime moment5 = gridTime.getMomentFromOffset(musicClock.getBeat(5).getRelativeDuration());
        LocalDateTime moment9 = gridTime.getMomentFromOffset(musicClock.getBeat(9).getRelativeDuration());


        when:
        bandedMusicTrack.startPlaying(moment5, wtfState)
        bandedMusicTrack.startPlaying(moment9, progressState)

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

        LocalDateTime moment5 = gridTime.getMomentFromOffset(musicClock.getBeat(5).getRelativeDuration());
        LocalDateTime moment7 = gridTime.getMomentFromOffset(musicClock.getBeat(7).getRelativeDuration());

        when:
        bandedMusicTrack.startPlaying(moment5, wtfState)
        bandedMusicTrack.stopPlaying(moment7)

        FeatureTag<IdeaFlowStateReference> rolloverTag = bandedMusicTrack.finish()

        then:
        assert wtfState == bandedMusicTrack.getFeatureAt(5)
        assert wtfState == bandedMusicTrack.getFeatureAt(6)
        assert wtfState == bandedMusicTrack.getFeatureAt(7)
        assert null == bandedMusicTrack.getFeatureAt(8)


    }


}
