package com.dreamscale.gridtime.core.security;

import com.dreamscale.gridtime.api.ResourcePaths;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.ForbiddenException;
import org.dreamscale.springboot.security.SecurityErrorCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Slf4j
public class AuthorizationFilter extends OncePerRequestFilter {

	@Autowired
	private RootAccountIdResolver rootAccountIdResolver;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		String apiKey = request.getHeader(ResourcePaths.API_KEY_HEADER);
		String connectionId = request.getHeader(ResourcePaths.CONNECT_ID_HEADER);
		RequestContext context = null;
		ApiKeyAuthenticationToken authentication = null;

		if (apiKey != null || connectionId != null) {
			UUID rootAccountId = lookupAccount(apiKey, connectionId);
			if (rootAccountId == null) {
				throw new ForbiddenException(SecurityErrorCodes.NOT_AUTHORIZED, "Failed to resolve user with apiKey=" + apiKey + " or connectId="+connectionId);
			}
			log.debug("Resolved user with apiKey={}, connectionId={}, rootAccountId={}", apiKey, connectionId, rootAccountId);
			authentication = createAuthenticationToken(apiKey, connectionId, rootAccountId);
			context = RequestContext.builder()
					.rootAccountId(rootAccountId)
					.requestUri((request.getRequestURI()))
					.build();
		}

		if (authentication != null) {
			SecurityContextHolder.getContext().setAuthentication(authentication);
			RequestContext.set(context);
		}

		try {
			filterChain.doFilter(request, response);
		} finally {
			SecurityContextHolder.getContext().setAuthentication(null);
			RequestContext.clear();
		}
	}

	private ApiKeyAuthenticationToken createAuthenticationToken(String apiKey, String connectionId, UUID rootAccountId) {
		String credential = apiKey != null ? apiKey : connectionId;
		AuthorityList authorities = new AuthorityList();
		// TODO: figure out how to determine user/org admin
		boolean isUser = true;
		boolean isOrgAdmin = false;
		if (isUser) {
			authorities.addRole(StandardRole.USER);
		}
		if (isOrgAdmin) {
			authorities.addRole(StandardRole.ORG_ADMIN);
		}
		return new ApiKeyAuthenticationToken(rootAccountId, credential, authorities);
	}

	private UUID lookupAccount(String apiKey, String connectionId) {
		UUID rootAccountId = null;

		if (apiKey != null) {
			rootAccountId = rootAccountIdResolver.findAccountIdByApiKey(apiKey);
		} else if (connectionId != null) {
			rootAccountId = rootAccountIdResolver.findAccountIdByConnectionId(connectionId);
		}

		return rootAccountId;
	}

}
