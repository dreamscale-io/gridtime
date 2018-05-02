package com.dreamscale.htmflow.core.security;

import com.dreamscale.htmflow.api.ResourcePaths;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.ForbiddenException;
import org.dreamscale.springboot.security.SecurityErrorCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
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
	private RequestContext invocationContext;
	@Autowired
	private UserIdResolver userIdResolver;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		RequestContext context = null;

		if (requiresApiKey(request) && notOptionsRequest(request)) {
			logger.debug("Checking API-Key...");

			String apiKey = request.getHeader(ResourcePaths.API_KEY_HEADER);
			if (apiKey == null) {
				throw new ForbiddenException(SecurityErrorCodes.MISSING_OR_INVALID_AUTHORIZATION_TOKEN, "Missing API key, header=" + ResourcePaths.API_KEY_HEADER);
			}

			UUID accountId = userIdResolver.findAccountIdByApiKey(apiKey);
			if (accountId == null) {
				throw new ForbiddenException(SecurityErrorCodes.NOT_AUTHORIZED, "Failed to resolve user with apiKey=" + apiKey);
			}
			context = RequestContext.builder()
					.userId(accountId)
					.build();
		}

		try {
			if (context != null) {
				RequestContext.set(context);
			}
			filterChain.doFilter(request, response);
		} finally {
			RequestContext.clear();
		}
	}

	private boolean notOptionsRequest(HttpServletRequest request) {
		return HttpMethod.OPTIONS.matches(request.getMethod()) == false;
	}

	private boolean requiresApiKey(HttpServletRequest request) {
		String servletPath = request.getServletPath();

		return (servletPath != null &&
				servletPath.startsWith(ResourcePaths.ACCOUNT_PATH) &&
				!servletPath.equals(ResourcePaths.ACCOUNT_PATH + ResourcePaths.ACTIVATE_PATH));
	}
}
