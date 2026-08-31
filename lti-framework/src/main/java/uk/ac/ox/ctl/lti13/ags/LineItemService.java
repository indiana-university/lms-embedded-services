package uk.ac.ox.ctl.lti13.ags;

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

import com.nimbusds.jose.JOSEException;
import net.minidev.json.JSONObject;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.client.RestTemplate;
import uk.ac.ox.ctl.lti13.OAuth2Interceptor;
import uk.ac.ox.ctl.lti13.TokenRetriever;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcLaunchFlowToken;

import java.util.Collections;
import java.util.List;

public class LineItemService {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final TokenRetriever tokenRetriever;

    public LineItemService(ClientRegistrationRepository clientRegistrationRepository, TokenRetriever tokenRetriever) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.tokenRetriever = tokenRetriever;
    }

    public LineItem createLineItem(OidcLaunchFlowToken oAuth2AuthenticationToken, LineItem lineItem) {
        String lineItemsUrl = getLineItemsUrl(oAuth2AuthenticationToken);
        if (lineItemsUrl == null) {
            return null;
        }
        return doWithClient(oAuth2AuthenticationToken, client -> client.postForObject(lineItemsUrl, lineItem, LineItem.class));
    }

    public List<LineItem> getLineItems(OidcLaunchFlowToken oAuth2AuthenticationToken) {
        String lineItemsUrl = getLineItemsUrl(oAuth2AuthenticationToken);
        if (lineItemsUrl == null) {
            return null;
        }
        return doWithClient(oAuth2AuthenticationToken, client ->
                client.exchange(lineItemsUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<LineItem>>() {}).getBody());
    }

    public LineItem getLineItem(OidcLaunchFlowToken oAuth2AuthenticationToken, String lineItemUrl) {
        return doWithClient(oAuth2AuthenticationToken, client -> client.getForObject(lineItemUrl, LineItem.class));
    }

    public LineItem updateLineItem(OidcLaunchFlowToken oAuth2AuthenticationToken, LineItem lineItem) {
        return doWithClient(oAuth2AuthenticationToken, client -> {
            client.put(lineItem.getId(), lineItem);
            return client.getForObject(lineItem.getId(), LineItem.class);
        });
    }

    public void deleteLineItem(OidcLaunchFlowToken oAuth2AuthenticationToken, String lineItemUrl) {
        doWithClient(oAuth2AuthenticationToken, client -> {
            client.delete(lineItemUrl);
            return null;
        });
    }

    private String getLineItemsUrl(OidcLaunchFlowToken oAuth2AuthenticationToken) {
        OidcUser principal = oAuth2AuthenticationToken.getPrincipal();
        if (principal != null) {
            Object o = principal.getClaims().get(AgsScopes.LTI_AGS_CLAIM);
            if (o instanceof JSONObject) {
                JSONObject json = (JSONObject) o;
                String lineItemsUrl = json.getAsString("lineitems");
                if (lineItemsUrl != null && !lineItemsUrl.isEmpty()) {
                    return lineItemsUrl;
                }
            }
        }
        return null;
    }

    private <T> T doWithClient(OidcLaunchFlowToken oAuth2AuthenticationToken, ClientAction<T> action) {
        String clientRegistrationId = oAuth2AuthenticationToken.getClientRegistration().getRegistrationId();
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(clientRegistrationId);
        if (clientRegistration == null) {
            throw new IllegalStateException("Failed to find client registration for: " + clientRegistrationId);
        }
        try {
            OAuth2AccessTokenResponse token = tokenRetriever.getToken(clientRegistration, AgsScopes.LTI_AGS_LINEITEM_SCOPE);
            RestTemplate client = new RestTemplate();
            client.setInterceptors(Collections.singletonList(new OAuth2Interceptor(token.getAccessToken())));
            return action.execute(client);
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to sign JWT", e);
        }
    }

    @FunctionalInterface
    private interface ClientAction<T> {
        T execute(RestTemplate client) throws JOSEException;
    }
}