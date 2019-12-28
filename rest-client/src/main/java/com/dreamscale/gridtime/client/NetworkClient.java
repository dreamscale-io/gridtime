package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.circuit.*;
import com.dreamscale.gridtime.api.event.NewSnippetEvent;
import com.dreamscale.gridtime.api.network.MemberChannelsDto;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface NetworkClient {

    @RequestLine("POST " + ResourcePaths.NETWORK_PATH + ResourcePaths.AUTH_PATH + ResourcePaths.MEMBER_PATH + "/{id}" )
    MemberChannelsDto authorizeMemberToUseNetwork(@Param("id") String memberId);

    @RequestLine("GET " + ResourcePaths.NETWORK_PATH + ResourcePaths.CIRCUIT_PATH)
    List<LearningCircuitDto> getAllOpenCircles();

    @RequestLine("GET " + ResourcePaths.NETWORK_PATH + ResourcePaths.CIRCUIT_PATH + ResourcePaths.ACTIVE_PATH )
    LearningCircuitDto getActiveCircle();

    @RequestLine("GET " + ResourcePaths.NETWORK_PATH + ResourcePaths.CIRCUIT_PATH + ResourcePaths.DO_IT_LATER_PATH)
    List<LearningCircuitDto> getAllDoItLaterCircles();

    @RequestLine("POST " + ResourcePaths.NETWORK_PATH + ResourcePaths.CIRCUIT_PATH)
    LearningCircuitDto createNewAdhocWTFCircle(CreateWTFCircleInputDto circleSessionInputDto);

    @RequestLine("POST " + ResourcePaths.NETWORK_PATH + ResourcePaths.CIRCUIT_PATH + "/{id}" + ResourcePaths.FEED_PATH + ResourcePaths.CHAT_PATH)
    CircuitMessageDto postChatMessageToCircleFeed(@Param("id") String circleId, ChatMessageInputDto chatMessageInputDto);

    @RequestLine("POST " + ResourcePaths.NETWORK_PATH + ResourcePaths.CIRCUIT_PATH + "/{id}" + ResourcePaths.FEED_PATH + ResourcePaths.SCREENSHOT_PATH)
    CircuitMessageDto postScreenshotReferenceToCircleFeed(@Param("id") String circleId, ScreenshotReferenceInputDto screenshotReferenceInputDto);

    @RequestLine("POST " + ResourcePaths.NETWORK_PATH + ResourcePaths.CIRCUIT_PATH + ResourcePaths.ACTIVE_PATH + ResourcePaths.FEED_PATH + ResourcePaths.SNIPPET_PATH)
    CircuitMessageDto postSnippetToActiveCircleFeed(NewSnippetEvent newSnippetEvent);

    @RequestLine("GET " + ResourcePaths.NETWORK_PATH + ResourcePaths.CIRCUIT_PATH + "/{id}" + ResourcePaths.FEED_PATH)
    List<CircuitMessageDto> getAllMessagesForCircleFeed(@Param("id") String circleId );

    @RequestLine("POST " + ResourcePaths.NETWORK_PATH + ResourcePaths.CIRCUIT_PATH + "/{id}" + ResourcePaths.TRANSITION_PATH + ResourcePaths.CLOSE_PATH)
    LearningCircuitDto closeCircle(@Param("id") String circleId);

    @RequestLine("POST " + ResourcePaths.NETWORK_PATH + ResourcePaths.CIRCUIT_PATH + "/{id}" + ResourcePaths.TRANSITION_PATH + ResourcePaths.DO_IT_LATER_PATH)
    LearningCircuitDto shelveCircleWithDoItLater(@Param("id") String circleId);

    @RequestLine("POST " + ResourcePaths.NETWORK_PATH + ResourcePaths.CIRCUIT_PATH + "/{id}" + ResourcePaths.TRANSITION_PATH + ResourcePaths.RESUME_PATH)
    LearningCircuitDto resumeAnExistingCircleFromDoItLaterShelf(@Param("id") String circleId);
}
