package com.dreamscale.gridtime.core.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestContext {

    private static final ThreadLocal<RequestContext> threadLocal = new ThreadLocal<>();

    public static RequestContext get() {
        return threadLocal.get();
    }

    public static void set(RequestContext requestContext) {
        threadLocal.set(requestContext);
    }

    public static void clear() {
        threadLocal.remove();
    }

    private UUID rootAccountId;

    private String requestUri;

}
