package edu.iu.uits.lms.canvas.security;

/*-
 * #%L
 * LMS Canvas Services
 * %%
 * Copyright (C) 2015 - 2021 Indiana University
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the Indiana University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.ClientAuthorizationRequiredException;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;

/**
 * Resolves the current LTI-launched user's Canvas OAuth2 access token (refreshing it via the
 * framework-managed {@link OAuth2AuthorizedClientManager} if needed) and sets the Bearer header
 * per-request. Parallel to {@link CanvasTokenAuthorizationInterceptor}, but per-user instead of
 * the static admin token.
 * <p>
 * Must be invoked on the original request thread: it reads both {@link SecurityContextHolder}
 * and {@link RequestContextHolder} state, which are ThreadLocal-backed, so calling it from an
 * {@code @Async} method or a scheduled job will fail unpredictably.
 */
public class CanvasOAuth2TokenInterceptor implements ClientHttpRequestInterceptor {

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final String registrationId;

    /**
     * Create a new interceptor which resolves and adds a Bearer authorization header for the
     * current user's Canvas OAuth2 access token.
     * @param authorizedClientManager the manager responsible for authorizing/refreshing the client
     * @param registrationId the Canvas OAuth2 client registration id to resolve
     */
    public CanvasOAuth2TokenInterceptor(OAuth2AuthorizedClientManager authorizedClientManager, String registrationId) {
        Assert.notNull(authorizedClientManager, "authorizedClientManager must not be null");
        Assert.hasLength(registrationId, "registrationId must not be empty");
        this.authorizedClientManager = authorizedClientManager;
        this.registrationId = registrationId;
    }

    /**
     * {@inheritDoc}
     * @throws ClientAuthorizationRequiredException if no authorized client can be resolved for
     * the current principal (e.g. the user has not yet completed the OAuth2 consent flow). This
     * is an unchecked exception that is NOT caught by Spring Security's
     * {@code ExceptionTranslationFilter}; it propagates as-is out of RestTemplate to the caller.
     */
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                         ClientHttpRequestExecution execution) throws IOException {
        Authentication principal = SecurityContextHolder.getContext().getAuthentication();

        OAuth2AuthorizeRequest.Builder authorizeRequestBuilder = OAuth2AuthorizeRequest
              .withClientRegistrationId(registrationId)
              .principal(principal);

        ServletRequestAttributes servletRequestAttributes =
              (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (servletRequestAttributes != null) {
            authorizeRequestBuilder
                  .attribute(HttpServletRequest.class.getName(), servletRequestAttributes.getRequest())
                  .attribute(HttpServletResponse.class.getName(), servletRequestAttributes.getResponse());
        }

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequestBuilder.build());
        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            throw new ClientAuthorizationRequiredException(registrationId);
        }

        request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + authorizedClient.getAccessToken().getTokenValue());
        return execution.execute(request, body);
    }
}
