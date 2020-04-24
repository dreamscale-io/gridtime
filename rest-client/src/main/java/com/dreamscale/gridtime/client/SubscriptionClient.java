package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.organization.*;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface SubscriptionClient {

    @RequestLine("GET " + ResourcePaths.SUBSCRIPTION_PATH)
    List<OrganizationSubscriptionDto> getOrganizationSubscriptions();

    @RequestLine("POST " + ResourcePaths.SUBSCRIPTION_PATH)
    OrganizationSubscriptionDto createSubscription(SubscriptionInputDto orgSubscriptionInputDto);

    @RequestLine("POST " + ResourcePaths.SUBSCRIPTION_PATH + "/{subscriptionId}" + ResourcePaths.CANCEL_PATH)
    OrganizationSubscriptionDto cancelSubscription(@Param("subscriptionId") String subscriptionId);

}
