package com.dreamscale.htmflow.resources;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.spirit.*;
import com.dreamscale.htmflow.core.domain.OrganizationMemberEntity;
import com.dreamscale.htmflow.core.security.RequestContext;
import com.dreamscale.htmflow.core.service.OrganizationService;
import com.dreamscale.htmflow.core.service.SpiritService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.SPIRIT_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class SpiritResource {

    @Autowired
    SpiritService spiritService;

    @Autowired
    OrganizationService organizationService;

    /**
     * Gets the latest state for the user's spirit
     * @return SpiritDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.ME_PATH )
    public SpiritDto getMySpirit() {
        RequestContext context = RequestContext.get();
        log.info("getMySpirit, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingSpirit = organizationService.getDefaultMembership(context.getMasterAccountId());
        return spiritService.getMySpirit(invokingSpirit.getOrganizationId(), invokingSpirit.getId());
    }

    /**
     * Queries for active spirit network, within 1 hop reach by default
     * @return SpiritNetworkDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.ME_PATH + ResourcePaths.NETWORK_PATH )
    public SpiritNetworkDto getMySpiritNetwork() {
        RequestContext context = RequestContext.get();
        log.info("getMySpiritNetwork, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingSpirit = organizationService.getDefaultMembership(context.getMasterAccountId());
        return spiritService.getSpiritNetwork(invokingSpirit.getOrganizationId(), invokingSpirit.getId());
    }

    /**
     * Gets the latest state for the another friend's spirit
     * @return SpiritDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{id}" )
    public SpiritDto getFriendSpirit(@PathVariable("id") String spiritId) {
        RequestContext context = RequestContext.get();
        log.info("getFriendSpirit, user={}", context.getMasterAccountId());

        UUID friendSpiritId = UUID.fromString(spiritId);

        OrganizationMemberEntity invokingSpirit = organizationService.getDefaultMembership(context.getMasterAccountId());
        organizationService.validateMemberWithinOrgByMemberId(invokingSpirit.getOrganizationId(), friendSpiritId);

        return spiritService.getFriendSpirit(invokingSpirit.getOrganizationId(), invokingSpirit.getId(), friendSpiritId);
    }

    /**
     * Gets the latest network for the another friend's spirit
     * @return SpiritNetworkDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{id}"+ ResourcePaths.NETWORK_PATH )
    public SpiritNetworkDto getFriendSpiritNetwork(@PathVariable("id") String spiritId) {
        RequestContext context = RequestContext.get();
        log.info("getFriendSpiritNetwork, user={}", context.getMasterAccountId());

        UUID friendSpiritId = UUID.fromString(spiritId);

        OrganizationMemberEntity invokingSpirit = organizationService.getDefaultMembership(context.getMasterAccountId());
        organizationService.validateMemberWithinOrgByMemberId(invokingSpirit.getOrganizationId(), friendSpiritId);

        return spiritService.getSpiritNetwork(invokingSpirit.getOrganizationId(), friendSpiritId);
    }

    /**
     * Create a spirit link to another Torchie, causing all stories to be shared, all XP to be shared,
     * and generating a multicast observable feed, for all shared events.
     * For example, use this feature when pair programming (or mob programming),
     * to multicast Journal entries across the linked spirits in your network, and share XP
     * @return ActiveSpiritNetworkDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{id}" + ResourcePaths.TRANSITION_PATH + ResourcePaths.LINK_PATH)
    public ActiveLinksNetworkDto linkToSpirit(@PathVariable("id") String spiritId) {
        RequestContext context = RequestContext.get();
        log.info("linkToSpirit, user={}", context.getMasterAccountId());

        UUID friendSpiritId = UUID.fromString(spiritId);

        OrganizationMemberEntity invokingSpirit = organizationService.getDefaultMembership(context.getMasterAccountId());
        organizationService.validateMemberWithinOrgByMemberId(invokingSpirit.getOrganizationId(), friendSpiritId);

        return spiritService.linkToSpirit(invokingSpirit.getOrganizationId(), invokingSpirit.getId(), friendSpiritId);
    }

    /**
     * Unlinks a spirit link to another Torchie from your active network.
     *
     * For example, you can use this feature if you're in a mob programming session, and someone leaves,
     * or if you're ending a pair programming session, and wanting to go back to working as an individual,
     * or if you just accidentally started a pairing session, and didn't mean to.
     *
     * @return ActiveSpiritNetworkDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{id}" + ResourcePaths.TRANSITION_PATH + ResourcePaths.UNLINK_PATH)
    public ActiveLinksNetworkDto unlinkSpirit(@PathVariable("id") String spiritId) {
        RequestContext context = RequestContext.get();
        log.info("unlinkSpirit, user={}", context.getMasterAccountId());

        UUID friendSpiritId = UUID.fromString(spiritId);

        OrganizationMemberEntity invokingSpirit = organizationService.getDefaultMembership(context.getMasterAccountId());
        organizationService.validateMemberWithinOrgByMemberId(invokingSpirit.getOrganizationId(), friendSpiritId);

        return spiritService.unlinkSpirit(invokingSpirit.getOrganizationId(), invokingSpirit.getId(), friendSpiritId);
    }

    /**
     * Remove yourself from your active spirit network.  If there are multiple members in the spirit network,
     * the network will go on without you.  If you are only linked to one other Torchie,
     * the network will dissolves, as soon as you unlink.
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.ME_PATH + ResourcePaths.TRANSITION_PATH + ResourcePaths.UNLINK_PATH)
    public void unlinkMe() {
        RequestContext context = RequestContext.get();
        log.info("unlinkMe, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingSpirit = organizationService.getDefaultMembership(context.getMasterAccountId());

        spiritService.unlinkMe(invokingSpirit.getOrganizationId(), invokingSpirit.getId());
    }

    /**
     * RIP your Torchie, and reset his XP back to level 1.  Your current stats and accomplishments
     * will be saved with his tombstone, along with an epitaph message.  As your Torchie lives multiple 'lives',
     * you can continue to view all of your past accomplishments on all the tombstones.
     *
     * Use this feature when a new team member joins your team, and you want to reset all the XP
     * to make the team game play more fun, so that everyone is around the same level.
     */

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.ME_PATH + ResourcePaths.TRANSITION_PATH + ResourcePaths.RIP_PATH)
    public TorchieTombstoneDto restInPeace(@RequestBody TombstoneInputDto tombStoneInputDto) {
        RequestContext context = RequestContext.get();
        log.info("restInPeace, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingSpirit = organizationService.getDefaultMembership(context.getMasterAccountId());

        return spiritService.restInPeace(context.getMasterAccountId(), invokingSpirit.getOrganizationId(), invokingSpirit.getId(), tombStoneInputDto.getEpitaph());
    }

    /**
     * Gets a list of past tombstones for your Torchie
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.ME_PATH + ResourcePaths.RIP_PATH)
    public List<TorchieTombstoneDto> getMyTombstones() {
        RequestContext context = RequestContext.get();
        OrganizationMemberEntity invokingSpirit = organizationService.getDefaultMembership(context.getMasterAccountId());

        return spiritService.getMyTombstones(invokingSpirit.getOrganizationId(), invokingSpirit.getId());

    }




}
