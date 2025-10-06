package uk.ac.ox.ctl.lti13.security.oauth2.client.lti.web;

/*-
 * #%L
 * LMS Canvas LTI Framework Services
 * %%
 * Copyright (C) 2015 - 2025 Indiana University
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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.function.BiConsumer;

/**
 * This store uses the state value in the initial request to lookup the request when the client
 * returns. Normally this would expose the login to a CSRF attack but we also check that the
 * remote IP address is the same in an attempt to limit this.
 * 
 * @see org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository
 */
public final class StateAuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    /**
     * The key we use to store the remote IP in attributes.
     */
    public static final String REMOTE_IP = "remote_ip";
    
    // The cache of request in flight
    private final Cache<String, OAuth2AuthorizationRequest> store;

    // Should we limit the login to a single IP address.
    // This may cause problems when users are on mobile devices and subsequent requests don't use the same IP address.
    private boolean limitIpAddress = true;

    // The handler to be called when an IP address mismatch is detected, by default this doesn't do anything.
    // It is expected that this will do something like logging of the mismatch.
    private BiConsumer<String, String> ipMismatchHandler = (a,b) -> {};

    public StateAuthorizationRequestRepository(Duration duration) {
        store = CacheBuilder.newBuilder()
                .expireAfterAccess(duration)
                .build();
    }

    public void setLimitIpAddress(boolean limitIpAddress) {
        this.limitIpAddress = limitIpAddress;
    }

    public void setIpMismatchHandler(BiConsumer<String, String> ipMismatchHandler) {
        this.ipMismatchHandler = ipMismatchHandler;
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Assert.notNull(request, "request cannot be null");
        String stateParameter = request.getParameter(OAuth2ParameterNames.STATE);
        if (stateParameter == null) {
            return null;
        }
        OAuth2AuthorizationRequest oAuth2AuthorizationRequest = store.getIfPresent(stateParameter);
        if (oAuth2AuthorizationRequest != null) {
            // The IP address from the initial request
            String initialIp = oAuth2AuthorizationRequest.getAttribute(REMOTE_IP);
            if (initialIp != null) {
                String requestIp = request.getRemoteAddr();
                if (!initialIp.equals(request.getRemoteAddr())) {
                    // Even if we aren't limiting IP address we call the consumer.
                    ipMismatchHandler.accept(initialIp, requestIp);
                    if (limitIpAddress) {
                        return null;
                    }
                }
            }
        } 
        return oAuth2AuthorizationRequest;
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        Assert.notNull(request, "request cannot be null");
        Assert.notNull(response, "response cannot be null");
        if (authorizationRequest == null) {
            this.removeAuthorizationRequest(request, response);
            return;
        }
        String state = authorizationRequest.getState();
        Assert.hasText(state, "authorizationRequest.state cannot be empty");
        store.put(state, authorizationRequest);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        if (authorizationRequest != null) {
            String stateParameter = request.getParameter(OAuth2ParameterNames.STATE);
            store.invalidate(stateParameter);
        }
        return authorizationRequest;
    }
}
