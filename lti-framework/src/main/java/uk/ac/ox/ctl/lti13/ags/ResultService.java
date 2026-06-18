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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ox.ctl.lti13.OAuth2Interceptor;
import uk.ac.ox.ctl.lti13.TokenRetriever;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcLaunchFlowToken;

import java.util.Collections;
import java.util.List;

public class ResultService {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final TokenRetriever tokenRetriever;

    public ResultService(ClientRegistrationRepository clientRegistrationRepository, TokenRetriever tokenRetriever) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.tokenRetriever = tokenRetriever;
    }

    public List<AGSResult> getResults(OidcLaunchFlowToken oAuth2AuthenticationToken, String lineItemUrl) {
        String url = lineItemUrl + "/results";
        return doWithClient(oAuth2AuthenticationToken, client ->
                client.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<AGSResult>>() {}).getBody());
    }

    public List<AGSResult> getResults(OidcLaunchFlowToken oAuth2AuthenticationToken, String lineItemUrl, String userId) {
        String url = UriComponentsBuilder.fromUriString(lineItemUrl + "/results")
                .queryParam("user_id", userId)
                .build().toUriString();
        return doWithClient(oAuth2AuthenticationToken, client ->
                client.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<AGSResult>>() {}).getBody());
    }

    public AGSResult getResult(OidcLaunchFlowToken oAuth2AuthenticationToken, String resultUrl) {
        return doWithClient(oAuth2AuthenticationToken, client -> client.getForObject(resultUrl, AGSResult.class));
    }

    private <T> T doWithClient(OidcLaunchFlowToken oAuth2AuthenticationToken, ClientAction<T> action) {
        String clientRegistrationId = oAuth2AuthenticationToken.getClientRegistration().getRegistrationId();
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(clientRegistrationId);
        if (clientRegistration == null) {
            throw new IllegalStateException("Failed to find client registration for: " + clientRegistrationId);
        }
        try {
            OAuth2AccessTokenResponse token = tokenRetriever.getToken(clientRegistration, AgsScopes.LTI_AGS_RESULT_READONLY_SCOPE);
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