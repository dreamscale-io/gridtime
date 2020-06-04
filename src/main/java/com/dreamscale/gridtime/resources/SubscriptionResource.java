package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.organization.*;
import com.dreamscale.gridtime.core.capability.membership.OrganizationCapability;
import com.dreamscale.gridtime.core.security.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.SUBSCRIPTION_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class SubscriptionResource {

    @Autowired
    private OrganizationCapability organizationCapability;


    /**
     * Retrieves all the organization subscriptions *owned* by this user
     *
     * @return List<OrganizationSubscriptionDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping()
    public List<OrganizationSubscriptionDto> getSubscriptions() {

        RequestContext context = RequestContext.get();
        log.info("getSubscriptions, user={}", context.getRootAccountId());

        return organizationCapability.getOrganizationSubscriptions(context.getRootAccountId());
    }

    /**
     * Creates a new organization subscription with the specified name, domain, and a valid paymentMethod.id from Stripe
     *
     * Once the organization is configured, additional configurations can be set by the owner
     *
     * @return OrganizationSubscriptionDto
     */

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping()
    public OrganizationSubscriptionDto createOrganizationSubscription(@RequestBody SubscriptionInputDto orgInputDto) {

        RequestContext context = RequestContext.get();
        log.info("createSubscription, user={}", context.getRootAccountId());

        return organizationCapability.createOrganizationSubscription(context.getRootAccountId(), orgInputDto);
    }

    /**
     * Retrieves all the organization subscriptions *owned* by this user
     *
     * @return List<OrganizationSubscriptionDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{subscriptionId}" )
    public OrganizationSubscriptionDto getSubscription(@PathVariable("subscriptionId") String subscriptionIdStr) {

        RequestContext context = RequestContext.get();
        log.info("getSubscription, user={}", context.getRootAccountId());

        UUID subscriptionId = UUID.fromString(subscriptionIdStr);

        return organizationCapability.getOrganizationSubscription(context.getRootAccountId(), subscriptionId);
    }


    /**
     * Cancels an organizations subscription with the specified id
     *
     * Must be the *owner* of this organization to cancel the subscription
     *
     * Must validate over email, before the cancelation is final.
     *
     * @return OrganizationSubscriptionDto
     */

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{subscriptionId}" + ResourcePaths.CANCEL_PATH)
    public OrganizationSubscriptionDto cancelSubscription(@PathVariable("subscriptionId") String subscriptionIdStr) {

        RequestContext context = RequestContext.get();
        log.info("cancelSubscription, user={}", context.getRootAccountId());

        UUID subscriptionId = UUID.fromString(subscriptionIdStr);

        return organizationCapability.cancelSubscription(context.getRootAccountId(), subscriptionId);
    }





}
