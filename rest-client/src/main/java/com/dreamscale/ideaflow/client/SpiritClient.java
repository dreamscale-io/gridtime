package com.dreamscale.ideaflow.client;

import com.dreamscale.ideaflow.api.ResourcePaths;
import com.dreamscale.ideaflow.api.spirit.*;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface SpiritClient {

    @RequestLine("GET " + ResourcePaths.SPIRIT_PATH + ResourcePaths.ME_PATH)
    SpiritDto getMySpirit();

    @RequestLine("GET " + ResourcePaths.SPIRIT_PATH + ResourcePaths.ME_PATH + ResourcePaths.NETWORK_PATH)
    SpiritNetworkDto getMySpiritNetwork();

    @RequestLine("GET " + ResourcePaths.SPIRIT_PATH + "/{id}")
    SpiritDto getFriendSpirit(@Param("id") String spiritId);

    @RequestLine("GET " + ResourcePaths.SPIRIT_PATH + "/{id}" + ResourcePaths.NETWORK_PATH)
    SpiritNetworkDto getFriendSpiritNetwork(@Param("id") String spiritId);

    @RequestLine("POST " + ResourcePaths.SPIRIT_PATH + "/{id}" + ResourcePaths.TRANSITION_PATH + ResourcePaths.LINK_PATH)
    ActiveLinksNetworkDto linkToSpirit(@Param("id") String spiritId);

    @RequestLine("POST " + ResourcePaths.SPIRIT_PATH + "/{id}" + ResourcePaths.TRANSITION_PATH + ResourcePaths.UNLINK_PATH)
    ActiveLinksNetworkDto unlinkSpirit(@Param("id") String spiritId);

    @RequestLine("POST " + ResourcePaths.SPIRIT_PATH + ResourcePaths.ME_PATH + ResourcePaths.TRANSITION_PATH + ResourcePaths.UNLINK_PATH)
    void unlinkMe();

    @RequestLine("POST " + ResourcePaths.SPIRIT_PATH + ResourcePaths.ME_PATH + ResourcePaths.TRANSITION_PATH + ResourcePaths.RIP_PATH)
    TorchieTombstoneDto restInPeace(TombstoneInputDto tombStoneInputDto);

    @RequestLine("GET " + ResourcePaths.SPIRIT_PATH + ResourcePaths.ME_PATH + ResourcePaths.RIP_PATH)
    List<TorchieTombstoneDto> getMyTombstones();


}
