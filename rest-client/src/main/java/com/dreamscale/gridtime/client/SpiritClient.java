package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.spirit.*;
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
    SpiritDto getMyTorchie();

    @RequestLine("POST " + ResourcePaths.SPIRIT_PATH + ResourcePaths.ME_PATH + ResourcePaths.XP_PATH)
    XPDto grantXP(XPDto xpAmount);

    @RequestLine("POST " + ResourcePaths.SPIRIT_PATH + ResourcePaths.GROUP_PATH + ResourcePaths.XP_PATH)
    XPDto grantGroupXP(XPDto xpAmount);

    @RequestLine("GET " + ResourcePaths.SPIRIT_PATH + ResourcePaths.ME_PATH + ResourcePaths.NETWORK_PATH)
    SpiritNetworkDto getMySpiritNetwork();

    @RequestLine("GET " + ResourcePaths.SPIRIT_PATH + "/{id}")
    SpiritDto getFriendTorchie(@Param("id") String torchieId);




    @RequestLine("GET " + ResourcePaths.SPIRIT_PATH + "/{id}" + ResourcePaths.NETWORK_PATH)
    SpiritNetworkDto getFriendSpiritNetwork(@Param("id") String torchieId);

    @RequestLine("POST " + ResourcePaths.SPIRIT_PATH + "/{id}" + ResourcePaths.TRANSITION_PATH + ResourcePaths.LINK_PATH)
    ActiveLinksNetworkDto linkToTorchie(@Param("id") String torchieId);

    @RequestLine("POST " + ResourcePaths.SPIRIT_PATH + "/{id}" + ResourcePaths.TRANSITION_PATH + ResourcePaths.UNLINK_PATH)
    ActiveLinksNetworkDto unlinkTorchie(@Param("id") String torchieId);

    @RequestLine("POST " + ResourcePaths.SPIRIT_PATH + ResourcePaths.ME_PATH + ResourcePaths.TRANSITION_PATH + ResourcePaths.UNLINK_PATH)
    void unlinkMe();

    @RequestLine("POST " + ResourcePaths.SPIRIT_PATH + ResourcePaths.ME_PATH + ResourcePaths.TRANSITION_PATH + ResourcePaths.RIP_PATH)
    TorchieTombstoneDto restInPeace(TombstoneInputDto tombStoneInputDto);

    @RequestLine("GET " + ResourcePaths.SPIRIT_PATH + ResourcePaths.ME_PATH + ResourcePaths.RIP_PATH)
    List<TorchieTombstoneDto> getMyTombstones();


}
