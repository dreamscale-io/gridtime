package com.dreamscale.htmflow.core.security;

import com.dreamscale.htmflow.api.ResourcePaths;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.ForbiddenException;
import org.dreamscale.springboot.security.SecurityErrorCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class AuthorizationFilter extends OncePerRequestFilter {

	@Autowired
	private RequestContext invocationContext;
	@Autowired
	private MasterAccountIdResolver masterAccountIdResolver;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		RequestContext context = null;
		ApiKeyAuthenticationToken authentication = null;

		if (requiresAuth(request) && notOptionsRequest(request)) {
			logger.debug("Checking API-Key...");

			String apiKey = request.getHeader(ResourcePaths.API_KEY_HEADER);
			String connectionId = request.getHeader(ResourcePaths.CONNECT_ID_HEADER);

			if (apiKey == null && connectionId == null) {
				throw new ForbiddenException(SecurityErrorCodes.MISSING_OR_INVALID_AUTHORIZATION_TOKEN, "Missing API key, header=" + ResourcePaths.API_KEY_HEADER);
			}

			UUID masterAccountId = lookupAccount(apiKey, connectionId);
			if (masterAccountId == null) {
				throw new ForbiddenException(SecurityErrorCodes.NOT_AUTHORIZED, "Failed to resolve user with apiKey=" + apiKey + " or connectId="+connectionId);
			}
			log.debug("Resolved user with apiKey={}, connectionId={}, masterAccountId={}", apiKey, connectionId, masterAccountId);
			authentication = createAuthenticationToken(apiKey, connectionId, masterAccountId);
			context = RequestContext.builder()
					.masterAccountId(masterAccountId)
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

	private ApiKeyAuthenticationToken createAuthenticationToken(String apiKey, String connectionId, UUID masterAccountId) {
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
		return new ApiKeyAuthenticationToken(masterAccountId, credential, authorities);
	}

	private UUID lookupAccount(String apiKey, String connectionId) {
		UUID masterAccountId = null;

		if (apiKey != null) {
			masterAccountId = masterAccountIdResolver.findAccountIdByApiKey(apiKey);
		} else if (connectionId != null) {
			masterAccountId = masterAccountIdResolver.findAccountIdByConnectionId(connectionId);
		}

		return masterAccountId;
	}

	private boolean notOptionsRequest(HttpServletRequest request) {
		return HttpMethod.OPTIONS.matches(request.getMethod()) == false;
	}

	private boolean requiresAuth(HttpServletRequest request) {
		String servletPath = request.getServletPath();
		String method = request.getMethod();

		List<String> noAuthPostPaths = new ArrayList<>();
		noAuthPostPaths.add(ResourcePaths.ACCOUNT_PATH + ResourcePaths.ACTIVATE_PATH);
		noAuthPostPaths.add(ResourcePaths.ORGANIZATION_PATH);

		List<String> noAuthGetPaths = new ArrayList<>();
		noAuthGetPaths.add(ResourcePaths.ORGANIZATION_PATH + ResourcePaths.MEMBER_PATH + ResourcePaths.INVITATION_PATH);

		boolean needsAuth = true;
		if (method.equals("POST") && startsWithAny(servletPath, noAuthPostPaths)) {
			needsAuth = false;
		}
		if (method.equals("GET") && startsWithAny(servletPath, noAuthGetPaths)) {
			needsAuth = false;
		}
		return needsAuth;
	}

	private boolean startsWithAny(String servletPath, List<String> allHtmFlowPaths) {
		boolean startsWithPath = false;

		for (String htmFlowPath : allHtmFlowPaths) {
			if (servletPath.startsWith(htmFlowPath)) {
				startsWithPath = true;
				break;
			}
		}

		return startsWithPath;
	}


}
